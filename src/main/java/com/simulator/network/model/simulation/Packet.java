package com.simulator.network.model.simulation;

import com.simulator.network.model.devices.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un paquete que viaja a través de la red durante una simulación.
 * Almacena la ruta calculada (secuencia de dispositivos) y el estado actual del salto.
 * Esta clase es fundamental para la animación y lógica de transmisión de paquetes.
 */
public class Packet {

    private final Device origen;
    private final Device destino;
    private final List<Device> ruta;
    private int currentHopIndex;

    /**
     * Construye un nuevo paquete con la ruta determinada.
     *
     * @param origen  Dispositivo de origen del paquete.
     * @param destino Dispositivo de destino del paquete.
     * @param ruta    Lista ordenada de dispositivos desde origen hasta destino.
     * @throws IllegalArgumentException si alguno de los parámetros es null o la ruta es vacía.
     */
    public Packet(Device origen, Device destino, List<Device> ruta) {
        if (origen == null || destino == null || ruta == null || ruta.isEmpty()) {
            throw new IllegalArgumentException("Origen, destino y ruta no pueden ser null o vacíos.");
        }
        this.origen = origen;
        this.destino = destino;
        this.ruta = new ArrayList<>(ruta);
        this.currentHopIndex = 0;
    }

    /**
     * Obtiene el dispositivo de origen del paquete.
     *
     * @return Dispositivo de origen.
     */
    public Device getOrigen() {
        return origen;
    }

    /**
     * Obtiene el dispositivo de destino del paquete.
     *
     * @return Dispositivo de destino.
     */
    public Device getDestino() {
        return destino;
    }

    /**
     * Devuelve la ruta completa del paquete, desde el origen hasta el destino.
     *
     * @return Nueva lista con la secuencia de dispositivos de la ruta.
     */
    public List<Device> getRuta() {
        return new ArrayList<>(ruta);
    }

    /**
     * Retorna el índice actual de la ruta por donde va el paquete.
     * Si es 0, el paquete está en el nodo de origen.
     *
     * @return Índice en la lista de la ruta.
     */
    public int getCurrentHopIndex() {
        return currentHopIndex;
    }

    /**
     * Avanza el paquete al siguiente dispositivo en la ruta.
     * Si ya llegó al destino, no realiza ningún avance.
     */
    public void advanceHop() {
        if (!isArrived()) {
            currentHopIndex++;
        }
    }

    /**
     * Indica si el paquete ya ha llegado a su destino (último dispositivo en la ruta).
     *
     * @return true si el paquete se encuentra en el destino, false en caso contrario.
     */
    public boolean isArrived() {
        return currentHopIndex >= (ruta.size() - 1);
    }

    /**
     * Devuelve el dispositivo actual en el que se encuentra el paquete según la simulación.
     *
     * @return Dispositivo actual en la ruta.
     */
    public Device getCurrentDevice() {
        return ruta.get(currentHopIndex);
    }

    /**
     * Retorna una representación textual breve del estado actual del paquete.
     *
     * @return Cadena que describe origen, destino y salto actual.
     */
    @Override
    public String toString() {
        return String.format("Packet[origen=%s, destino=%s, hop=%d/%d]",
                origen.getId(), destino.getId(), currentHopIndex, ruta.size() - 1);
    }
}
