package com.simulator.network.model.simulation;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.graph.Link;
import com.simulator.network.model.graph.NetworkGraph;

import java.util.*;

/**
 * Estrategia de enrutamiento que implementa el algoritmo de Dijkstra
 * para encontrar la ruta de menor suma de latencias entre dos dispositivos de red.
 * Esta estrategia considera los pesos (latencias) de los enlaces para calcular la ruta óptima.
 */
public class DijkstraStrategy implements RoutingStrategy {

    /**
     * Calcula la ruta de menor latencia entre dos dispositivos de la red usando el algoritmo de Dijkstra.
     *
     * @param graph Grafo de red donde se realiza la búsqueda de rutas.
     * @param start Dispositivo de origen.
     * @param end   Dispositivo de destino.
     * @return Lista de dispositivos que conforman la ruta óptima desde {@code start} hasta {@code end},
     *         incluyendo ambos extremos. Retorna una lista vacía si no existe ruta.
     */
    @Override
    public List<Device> calculatePath(NetworkGraph graph, Device start, Device end) {
        if (graph == null || start == null || end == null) {
            return Collections.emptyList();
        }
        if (!graph.containsDevice(start) || !graph.containsDevice(end)) {
            return Collections.emptyList();
        }

        Map<Device, Double> dist = new HashMap<>();
        Map<Device, Device> prev = new HashMap<>();

        for (Device d : graph.getAllDevices()) {
            dist.put(d, Double.POSITIVE_INFINITY);
            prev.put(d, null);
        }
        dist.put(start, 0.0);

        PriorityQueue<Device> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
        pq.add(start);

        while (!pq.isEmpty()) {
            Device u = pq.poll();
            if (u.equals(end)) break;

            double distU = dist.get(u);
            for (Link link : graph.getLinksFrom(u)) {
                Device v = link.getTarget();
                double alt = distU + link.getLatency();
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        if (prev.get(end) == null && !end.equals(start)) {
            return Collections.emptyList();
        }

        LinkedList<Device> path = new LinkedList<>();
        Device cur = end;
        while (cur != null) {
            path.addFirst(cur);
            cur = prev.get(cur);
        }
        return path;
    }
}
