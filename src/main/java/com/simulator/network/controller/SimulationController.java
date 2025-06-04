package com.simulator.network.controller;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.graph.NetworkGraph;
import com.simulator.network.model.simulation.*;
import com.simulator.network.view.GraphView;

import java.util.List;

/**
 * Controlador responsable de iniciar y coordinar la simulación del envío de paquetes a través de la red.
 * Utiliza una estrategia de enrutamiento configurable para determinar la ruta que seguirá el paquete
 * y solicita a la vista que realice la animación correspondiente.
 */
public class SimulationController {

    private final NetworkGraph modelGraph;
    private final GraphView graphView;
    private RoutingStrategy routingStrategy;

    /**
     * Crea una nueva instancia del controlador de simulación.
     *
     * @param modelGraph Instancia del grafo de red que representa la topología actual.
     * @param graphView  Vista gráfica responsable de la animación del paquete.
     */
    public SimulationController(NetworkGraph modelGraph, GraphView graphView) {
        this.modelGraph = modelGraph;
        this.graphView = graphView;
        this.routingStrategy = new DijkstraStrategy();
    }

    /**
     * Permite establecer dinámicamente la estrategia de enrutamiento utilizada para calcular rutas.
     *
     * @param strategy Nueva estrategia de enrutamiento a utilizar.
     * @throws IllegalArgumentException Si la estrategia proporcionada es null.
     */
    public void setRoutingStrategy(RoutingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null.");
        }
        this.routingStrategy = strategy;
    }

    /**
     * Retorna la estrategia de enrutamiento actualmente configurada.
     *
     * @return Instancia de RoutingStrategy utilizada actualmente.
     */
    public RoutingStrategy getRoutingStrategy() {
        return routingStrategy;
    }

    /**
     * Inicia la simulación de envío de un paquete entre dos dispositivos especificados por sus identificadores.
     * No notifica cuando termina la animación.
     *
     * @param sourceId Identificador del nodo origen.
     * @param targetId Identificador del nodo destino.
     * @throws IllegalArgumentException Si los dispositivos no existen o no hay ruta entre ellos.
     */
    public void simulatePacket(String sourceId, String targetId) {
        simulatePacket(sourceId, targetId, null);
    }

    /**
     * Inicia la simulación de envío de un paquete entre dos dispositivos especificados.
     * Si se provee un listener, se notificará cuando la animación termine.
     *
     * @param sourceId Identificador del nodo origen.
     * @param targetId Identificador del nodo destino.
     * @param listener Listener a notificar al finalizar la simulación (puede ser null).
     * @throws IllegalArgumentException Si los dispositivos no existen o no hay ruta entre ellos.
     */
    public void simulatePacket(String sourceId, String targetId, SimulationListener listener) {
        Device origen = findDeviceById(sourceId);
        Device destino = findDeviceById(targetId);

        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Origen o destino no existen en la topología.");
        }

        List<Device> ruta = routingStrategy.calculatePath(modelGraph, origen, destino);
        if (ruta == null || ruta.isEmpty()) {
            throw new IllegalArgumentException("No existe ruta entre " + sourceId + " y " + targetId);
        }

        Packet packet = new Packet(origen, destino, ruta);

        if (listener != null) {
            graphView.setOnSimulationFinishedListener(listener::onSimulationFinished);
        }

        graphView.animatePacket(packet);
    }

    /**
     * Alterna la estrategia de enrutamiento entre Dijkstra y BFS.
     * Si la actual es Dijkstra, cambia a BFS; en cualquier otro caso, cambia a Dijkstra.
     */
    public void toggleBetweenDijkstraAndBFS() {
        if (routingStrategy instanceof DijkstraStrategy) {
            this.routingStrategy = new BfsStrategy();
        } else {
            this.routingStrategy = new DijkstraStrategy();
        }
    }

    /**
     * Busca y retorna un dispositivo dentro del grafo usando su identificador.
     *
     * @param id Identificador único del dispositivo.
     * @return Instancia de Device si existe, o null si no se encuentra.
     */
    private Device findDeviceById(String id) {
        if (id == null) {
            return null;
        }
        for (Device d : modelGraph.getAllDevices()) {
            if (d.getId().equalsIgnoreCase(id)) {
                return d;
            }
        }
        return null;
    }
}
