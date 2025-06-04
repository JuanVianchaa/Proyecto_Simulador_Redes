package com.simulator.network.model.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.simulator.network.model.devices.Device;

import java.util.*;

/**
 * Representa la topología completa de la red como un grafo dirigido.
 * Gestiona dispositivos y enlaces, mantiene la estructura de adyacencia para consultas eficientes,
 * y soporta serialización/deserialización mediante Jackson.
 * <p>
 * Permite registrar listeners para notificar cambios en la topología, facilitando la integración con la vista.
 */
public class NetworkGraph {

    @JsonIgnore
    private final Map<Device, List<Link>> adjacency;

    @JsonIgnore
    private final List<NetworkChangeListener> listeners;

    /**
     * Construye un grafo vacío, sin dispositivos ni enlaces.
     */
    public NetworkGraph() {
        this.adjacency = new HashMap<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Devuelve el conjunto de todos los dispositivos presentes en la topología.
     * Utilizado por Jackson para serialización bajo la clave "allDevices".
     *
     * @return Conjunto inmodificable de dispositivos.
     */
    @JsonProperty("allDevices")
    public Set<Device> getAllDevicesView() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /**
     * Establece el conjunto de dispositivos de la topología. Usado por Jackson al deserializar.
     * Elimina los dispositivos existentes y registra los nuevos con listas vacías de enlaces.
     *
     * @param devices Conjunto de dispositivos a cargar.
     */
    @JsonProperty("allDevices")
    public void setAllDevicesView(Set<Device> devices) {
        adjacency.clear();
        if (devices != null) {
            for (Device d : devices) {
                adjacency.put(d, new ArrayList<>());
            }
        }
        notifyChange();
    }

    /**
     * Devuelve la lista completa de enlaces en la topología, agregando todos los enlaces de cada dispositivo.
     * Utilizado por Jackson para serialización bajo la clave "allLinks".
     *
     * @return Lista inmodificable de enlaces.
     */
    @JsonProperty("allLinks")
    public List<Link> getAllLinksView() {
        List<Link> all = new ArrayList<>();
        for (List<Link> links : adjacency.values()) {
            all.addAll(links);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Establece la lista completa de enlaces en la topología. Usado por Jackson al deserializar.
     * Limpia enlaces previos y agrega los nuevos, asegurando la existencia de ambos dispositivos.
     *
     * @param links Lista de enlaces a cargar.
     */
    @JsonProperty("allLinks")
    public void setAllLinksView(List<Link> links) {
        for (Device d : adjacency.keySet()) {
            adjacency.get(d).clear();
        }
        if (links != null) {
            for (Link link : links) {
                Device src = link.getSource();
                Device tgt = link.getTarget();
                double latency = link.getLatency();

                adjacency.putIfAbsent(src, new ArrayList<>());
                adjacency.putIfAbsent(tgt, new ArrayList<>());

                adjacency.get(src).add(new Link(src, tgt, latency));
            }
        }
        notifyChange();
    }

    /**
     * Registra un listener que será notificado cada vez que cambie la topología.
     *
     * @param listener Instancia de {@link NetworkChangeListener} (no debe ser null).
     */
    public void addListener(NetworkChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un listener previamente registrado.
     *
     * @param listener Listener a remover.
     */
    public void removeListener(NetworkChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica a todos los listeners registrados que la topología ha cambiado.
     */
    public void notifyChange() {
        for (NetworkChangeListener listener : listeners) {
            listener.onTopologyChanged();
        }
    }

    /**
     * Agrega un dispositivo a la topología. Si ya existe, no realiza ninguna acción.
     *
     * @param d Dispositivo a agregar (no debe ser null).
     * @throws IllegalArgumentException si el dispositivo es null.
     */
    public void addDevice(Device d) {
        if (d == null) {
            throw new IllegalArgumentException("El dispositivo no puede ser null.");
        }
        boolean existed = adjacency.containsKey(d);
        adjacency.putIfAbsent(d, new ArrayList<>());
        if (!existed) {
            notifyChange();
        }
    }

    /**
     * Elimina un dispositivo y todos sus enlaces entrantes y salientes.
     * Si el dispositivo no existe, no realiza ninguna acción.
     *
     * @param d Dispositivo a eliminar (no debe ser null).
     * @throws IllegalArgumentException si el dispositivo es null.
     */
    public void removeDevice(Device d) {
        if (d == null) {
            throw new IllegalArgumentException("El dispositivo no puede ser null.");
        }
        if (!adjacency.containsKey(d)) {
            return;
        }
        for (List<Link> links : adjacency.values()) {
            links.removeIf(link -> link.getTarget().equals(d));
        }
        adjacency.remove(d);
        notifyChange();
    }

    /**
     * Conecta dos dispositivos creando un enlace dirigido (a → b) con la latencia indicada.
     * Si algún dispositivo no existe, lo agrega primero. Si ya existe el enlace, actualiza la latencia.
     *
     * @param a       Dispositivo origen.
     * @param b       Dispositivo destino.
     * @param latency Latencia del enlace en milisegundos.
     * @throws IllegalArgumentException si alguno de los dispositivos es null.
     */
    public void connect(Device a, Device b, double latency) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Los dispositivos no pueden ser null.");
        }
        adjacency.putIfAbsent(a, new ArrayList<>());
        adjacency.putIfAbsent(b, new ArrayList<>());

        List<Link> listA = adjacency.get(a);
        boolean updated = false;
        for (Link link : listA) {
            if (link.getTarget().equals(b)) {
                link.setLatency(latency);
                updated = true;
                break;
            }
        }
        if (!updated) {
            listA.add(new Link(a, b, latency));
        }
        notifyChange();
    }

    /**
     * Desconecta (elimina) el enlace dirigido de a → b, si existe.
     * No afecta el enlace inverso.
     *
     * @param a Dispositivo origen.
     * @param b Dispositivo destino.
     * @throws IllegalArgumentException si alguno de los dispositivos es null.
     */
    public void disconnect(Device a, Device b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Los dispositivos no pueden ser null.");
        }
        if (!adjacency.containsKey(a) || !adjacency.containsKey(b)) {
            return;
        }
        List<Link> listA = adjacency.get(a);
        boolean removed = listA.removeIf(link -> link.getTarget().equals(b));
        if (removed) {
            notifyChange();
        }
    }

    /**
     * Devuelve el conjunto de todos los dispositivos registrados en la topología.
     *
     * @return Conjunto inmodificable de dispositivos.
     */
    public Set<Device> getAllDevices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /**
     * Devuelve una lista de todos los enlaces en la topología.
     *
     * @return Lista inmodificable de enlaces.
     */
    public List<Link> getAllLinks() {
        List<Link> all = new ArrayList<>();
        for (List<Link> links : adjacency.values()) {
            all.addAll(links);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Devuelve la lista de enlaces salientes desde un dispositivo dado.
     *
     * @param d Dispositivo origen.
     * @return Lista inmodificable de enlaces salientes, o vacía si no existen.
     */
    public List<Link> getLinksFrom(Device d) {
        List<Link> lista = adjacency.get(d);
        if (lista == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(lista);
    }

    /**
     * Devuelve la lista de dispositivos vecinos a un nodo, es decir,
     * todos los dispositivos destino alcanzables mediante enlaces salientes desde d.
     *
     * @param d Dispositivo de consulta.
     * @return Lista inmodificable de vecinos, o vacía si no existen.
     */
    public List<Device> getNeighbors(Device d) {
        List<Link> links = adjacency.get(d);
        if (links == null) {
            return Collections.emptyList();
        }
        List<Device> vecinos = new ArrayList<>();
        for (Link link : links) {
            vecinos.add(link.getTarget());
        }
        return Collections.unmodifiableList(vecinos);
    }

    /**
     * Retorna la latencia del enlace directo de a → b, si existe.
     * Si no existe, retorna {@link Double#POSITIVE_INFINITY}.
     *
     * @param a Dispositivo origen.
     * @param b Dispositivo destino.
     * @return Latencia del enlace, o infinito si no existe enlace directo.
     */
    public double getLatencyBetween(Device a, Device b) {
        List<Link> links = adjacency.get(a);
        if (links == null) {
            return Double.POSITIVE_INFINITY;
        }
        for (Link link : links) {
            if (link.getTarget().equals(b)) {
                return link.getLatency();
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Verifica si un dispositivo está presente en la topología.
     *
     * @param d Dispositivo a consultar.
     * @return true si el dispositivo existe, false en caso contrario.
     */
    public boolean containsDevice(Device d) {
        return adjacency.containsKey(d);
    }
}
