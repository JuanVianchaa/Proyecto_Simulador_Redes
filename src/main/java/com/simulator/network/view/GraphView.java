package com.simulator.network.view;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.graph.Link;
import com.simulator.network.model.graph.NetworkChangeListener;
import com.simulator.network.model.graph.NetworkGraph;
import com.simulator.network.model.simulation.Packet;
import com.simulator.network.model.simulation.SimulationListener;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista principal donde se dibuja la topología de red.
 * Extiende {@link Pane} y muestra los nodos como {@link DeviceNodeView}
 * y los enlaces como {@link LinkView}.
 * <p>
 * Implementa {@link NetworkChangeListener} para refrescarse automáticamente ante
 * cambios en la topología (dispositivos o enlaces) del modelo.
 * Soporta animación visual de paquetes viajando a través de la red.
 */
public class GraphView extends Pane implements NetworkChangeListener {

    private final NetworkGraph modelGraph;
    private final Map<Device, DeviceNodeView> nodeViews = new HashMap<>();
    private final Map<Link, LinkView> linkViews = new HashMap<>();
    private SimulationListener onSimulationFinishedListener;
    private final Circle packetCircle = new Circle(5);

    /**
     * Crea la vista gráfica para la topología del grafo dado.
     *
     * @param graph Instancia de {@link NetworkGraph} a visualizar.
     */
    public GraphView(NetworkGraph graph) {
        this.modelGraph = graph;
        this.packetCircle.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 1;");
        modelGraph.addListener(this);
        redraw();
    }

    /**
     * Método de la interfaz {@link NetworkChangeListener}.
     * Es invocado automáticamente cuando la topología de red cambia.
     * Refresca la vista reubicando o recreando nodos y enlaces.
     */
    @Override
    public void onTopologyChanged() {
        Platform.runLater(this::redraw);
    }

    /**
     * Redibuja toda la topología: nodos y enlaces.
     * Conserva posiciones de nodos existentes y posiciona aleatoriamente los nuevos.
     */
    private void redraw() {
        Map<Device, Point2D> oldPositions = new HashMap<>();
        for (Map.Entry<Device, DeviceNodeView> entry : nodeViews.entrySet()) {
            Device d = entry.getKey();
            DeviceNodeView view = entry.getValue();
            oldPositions.put(d, new Point2D(view.centerXProperty().get(), view.centerYProperty().get()));
        }

        getChildren().clear();
        nodeViews.clear();
        linkViews.clear();

        for (Device d : modelGraph.getAllDevices()) {
            Point2D pos = oldPositions.get(d);
            double x, y;
            if (pos != null) {
                x = pos.getX();
                y = pos.getY();
            } else {
                x = 100 + Math.random() * 400;
                y = 100 + Math.random() * 300;
            }
            DeviceNodeView nv = new DeviceNodeView(d, x, y);
            nodeViews.put(d, nv);
            getChildren().add(nv);
        }

        for (Device d : modelGraph.getAllDevices()) {
            List<Link> links = modelGraph.getLinksFrom(d);
            for (Link link : links) {
                if (link.getSource().equals(d)) {
                    DeviceNodeView srcView = nodeViews.get(link.getSource());
                    DeviceNodeView tgtView = nodeViews.get(link.getTarget());
                    if (srcView != null && tgtView != null) {
                        LinkView lv = new LinkView(link, srcView, tgtView);
                        linkViews.put(link, lv);
                        getChildren().add(0, lv);
                    }
                }
            }
        }
    }

    /**
     * Registra un {@link SimulationListener} que será llamado al finalizar
     * la animación completa del paquete.
     *
     * @param listener Callback para notificar finalización de la simulación.
     */
    public void setOnSimulationFinishedListener(SimulationListener listener) {
        this.onSimulationFinishedListener = listener;
    }

    /**
     * Inicia la animación visual del paquete viajando por la ruta especificada.
     * Al finalizar, invoca el listener registrado si existe.
     *
     * @param packet Instancia de {@link Packet} con la ruta a animar.
     */
    public void animatePacket(Packet packet) {
        List<Device> ruta = packet.getRuta();
        if (ruta == null || ruta.size() < 2) {
            if (onSimulationFinishedListener != null) {
                Platform.runLater(onSimulationFinishedListener::onSimulationFinished);
            }
            return;
        }

        if (!getChildren().contains(packetCircle)) {
            getChildren().add(packetCircle);
        }

        Device primero = ruta.get(0);
        double initX = getNodeX(primero);
        double initY = getNodeY(primero);
        packetCircle.setTranslateX(initX);
        packetCircle.setTranslateY(initY);

        SequentialTransition seq = new SequentialTransition();

        for (int i = 0; i < ruta.size() - 1; i++) {
            Device a = ruta.get(i);
            Device b = ruta.get(i + 1);

            Link link = findLinkBetween(a, b);
            if (link == null) {
                break;
            }

            DeviceNodeView srcView = nodeViews.get(a);
            DeviceNodeView tgtView = nodeViews.get(b);
            if (srcView == null || tgtView == null) {
                break;
            }

            double startX = getNodeX(a);
            double startY = getNodeY(a);
            double endX = getNodeX(b);
            double endY = getNodeY(b);

            Line pathLine = new Line(startX, startY, endX, endY);

            PathTransition pt = new PathTransition();
            pt.setNode(packetCircle);
            pt.setPath(pathLine);
            pt.setDuration(Duration.millis(link.getLatency()));

            seq.getChildren().add(pt);
        }

        seq.setOnFinished(evt -> {
            if (onSimulationFinishedListener != null) {
                onSimulationFinishedListener.onSimulationFinished();
            }
        });

        Platform.runLater(seq::play);
    }

    /**
     * Busca un enlace dirigido entre dos dispositivos en el modelo.
     *
     * @param source Dispositivo de origen.
     * @param target Dispositivo de destino.
     * @return Enlace existente, o null si no existe.
     */
    private Link findLinkBetween(Device source, Device target) {
        for (Link l : modelGraph.getLinksFrom(source)) {
            if (l.getTarget().equals(target)) {
                return l;
            }
        }
        return null;
    }

    /**
     * Obtiene la coordenada X (centro) del nodo asociado al dispositivo dado.
     *
     * @param d Dispositivo.
     * @return Coordenada X del nodo, o 0 si no existe.
     */
    private double getNodeX(Device d) {
        DeviceNodeView view = nodeViews.get(d);
        return (view == null) ? 0 : view.centerXProperty().get();
    }

    /**
     * Obtiene la coordenada Y (centro) del nodo asociado al dispositivo dado.
     *
     * @param d Dispositivo.
     * @return Coordenada Y del nodo, o 0 si no existe.
     */
    private double getNodeY(Device d) {
        DeviceNodeView view = nodeViews.get(d);
        return (view == null) ? 0 : view.centerYProperty().get();
    }
}
