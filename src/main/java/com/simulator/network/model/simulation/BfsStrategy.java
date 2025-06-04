package com.simulator.network.model.simulation;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.graph.NetworkGraph;

import java.util.*;

/**
 * Estrategia de enrutamiento basada en búsqueda en anchura (BFS).
 * Calcula la ruta más corta en número de saltos entre dos dispositivos,
 * ignorando las latencias o pesos de los enlaces.
 *
 * Esta implementación es útil para obtener rutas mínimas en grafos no ponderados,
 * donde solo interesa la cantidad de saltos.
 */
public class BfsStrategy implements RoutingStrategy {

    /**
     * Calcula la ruta más corta (menor número de saltos) entre dos dispositivos
     * utilizando búsqueda en anchura. Si no existe ruta, retorna una lista vacía.
     *
     * @param graph   Grafo de red donde se realiza la búsqueda.
     * @param origen  Dispositivo de origen.
     * @param destino Dispositivo de destino.
     * @return Lista de dispositivos que conforman la ruta (incluyendo origen y destino),
     *         o lista vacía si no existe ruta posible.
     * @throws IllegalArgumentException si alguno de los parámetros es null.
     */
    @Override
    public List<Device> calculatePath(NetworkGraph graph, Device origen, Device destino) {
        if (graph == null || origen == null || destino == null) {
            throw new IllegalArgumentException("Graph, origen y destino no pueden ser null.");
        }
        if (!graph.containsDevice(origen) || !graph.containsDevice(destino)) {
            return Collections.emptyList();
        }

        Queue<Device> queue = new LinkedList<>();
        Map<Device, Boolean> visited = new HashMap<>();
        Map<Device, Device> predecesor = new HashMap<>();

        for (Device d : graph.getAllDevices()) {
            visited.put(d, false);
            predecesor.put(d, null);
        }

        visited.put(origen, true);
        queue.offer(origen);

        boolean encontrado = false;
        while (!queue.isEmpty()) {
            Device actual = queue.poll();
            if (actual.equals(destino)) {
                encontrado = true;
                break;
            }
            for (Device vecino : graph.getNeighbors(actual)) {
                if (!visited.get(vecino)) {
                    visited.put(vecino, true);
                    predecesor.put(vecino, actual);
                    queue.offer(vecino);
                }
            }
        }

        if (!encontrado) {
            return Collections.emptyList();
        }

        List<Device> ruta = new LinkedList<>();
        Device paso = destino;
        while (paso != null) {
            ruta.add(0, paso);
            paso = predecesor.get(paso);
        }
        return ruta;
    }
}
