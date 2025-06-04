package com.simulator.network.view;

import com.simulator.network.model.graph.Link;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Vista gráfica que representa un enlace dirigido entre dos nodos de red.
 * Dibuja una línea que conecta dos {@link DeviceNodeView} y muestra la latencia asociada en el punto medio.
 * <p>
 * El texto de latencia se actualiza automáticamente si cambian las posiciones de los nodos.
 * Además, al pasar el cursor por la línea, se muestra un tooltip con la latencia.
 */
public class LinkView extends Group {

    private final Link model;
    private final DeviceNodeView sourceView;
    private final DeviceNodeView targetView;
    private final Line line;
    private final Text weightText;

    /**
     * Crea una nueva vista gráfica para el enlace entre dos nodos, con visualización de latencia.
     *
     * @param model      Instancia de {@link Link} que se representa.
     * @param sourceView Vista del nodo de origen.
     * @param targetView Vista del nodo de destino.
     */
    public LinkView(Link model, DeviceNodeView sourceView, DeviceNodeView targetView) {
        this.model = model;
        this.sourceView = sourceView;
        this.targetView = targetView;

        line = new Line();
        line.setStrokeWidth(2);
        if (model.getLatency() > 100) {
            line.setStroke(Color.RED); // Alta latencia en rojo
        } else {
            line.setStroke(Color.GREEN); // Baja latencia en verde
        }
        line.setMouseTransparent(false);
        line.setPickOnBounds(true);

        line.startXProperty().bind(sourceView.centerXProperty());
        line.startYProperty().bind(sourceView.centerYProperty());
        line.endXProperty().bind(targetView.centerXProperty());
        line.endYProperty().bind(targetView.centerYProperty());

        weightText = new Text(model.getLatency() + " ms");
        weightText.setFont(Font.font(11));
        weightText.setFill(Color.DARKRED);
        weightText.applyCss();
        weightText.autosize();

        Tooltip.install(line, new Tooltip("Latency: " + model.getLatency() + " ms"));

        getChildren().addAll(line, weightText);

        ChangeListener<Number> coordsListener = (obs, oldVal, newVal) -> updateTextPosition();
        line.startXProperty().addListener(coordsListener);
        line.startYProperty().addListener(coordsListener);
        line.endXProperty().addListener(coordsListener);
        line.endYProperty().addListener(coordsListener);

        updateTextPosition();
    }

    /**
     * Actualiza la posición del texto de latencia para centrarlo en el punto medio de la línea,
     * desplazado ligeramente hacia arriba para mejor visibilidad.
     */
    private void updateTextPosition() {
        double startX = line.getStartX();
        double startY = line.getStartY();
        double endX = line.getEndX();
        double endY = line.getEndY();

        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;

        double textWidth = weightText.getBoundsInLocal().getWidth();
        weightText.setX(midX - textWidth / 2.0);
        weightText.setY(midY - 4);
    }
}
