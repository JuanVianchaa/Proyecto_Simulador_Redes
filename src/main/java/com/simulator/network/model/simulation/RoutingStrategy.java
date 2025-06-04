package com.simulator.network.model.simulation;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.graph.NetworkGraph;

import java.util.List;

/**
 * Interfaz funcional que define una estrategia de enrutamiento para calcular
 * la ruta óptima entre dos dispositivos dentro de una topología de red representada por {@link NetworkGraph}.
 * <p>
 * Permite implementar distintos algoritmos de enrutamiento, como Dijkstra o BFS,
 * de manera intercambiable en el sistema.
 */
public interface RoutingStrategy {

    /**
     * Calcula la ruta óptima (lista ordenada de dispositivos) desde {@code origen} hasta {@code destino}
     * dentro del grafo {@code graph}. La ruta debe comenzar con el origen y terminar con el destino.
     * Si no existe ruta posible, puede retornar una lista vacía o {@code null}, según la implementación.
     *
     * @param graph   Grafo de red donde se busca la ruta.
     * @param origen  Dispositivo de origen.
     * @param destino Dispositivo de destino.
     * @return Lista de dispositivos que representan la ruta desde origen hasta destino,
     *         o lista vacía/null si no hay conexión posible.
     */
    List<Device> calculatePath(NetworkGraph graph, Device origen, Device destino);
}
