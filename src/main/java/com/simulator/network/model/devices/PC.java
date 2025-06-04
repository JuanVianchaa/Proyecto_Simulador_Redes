package com.simulator.network.model.devices;

/**
 * Representa un dispositivo de red de tipo PC (Computador Personal).
 * Esta clase concreta extiende {@link Device} y configura automáticamente su tipo {@link DeviceType#PC}.
 *
 * Se utiliza para modelar nodos terminales en la simulación de la red.
 */
public class PC extends Device {

    /**
     * Constructor por defecto requerido para la deserialización con Jackson.
     */
    public PC() {
        super();
    }

    /**
     * Crea una nueva instancia de PC con los datos proporcionados.
     *
     * @param id   Identificador único del dispositivo.
     * @param name Nombre descriptivo del dispositivo.
     */
    public PC(String id, String name) {
        super(id, name, DeviceType.PC);
    }
}
