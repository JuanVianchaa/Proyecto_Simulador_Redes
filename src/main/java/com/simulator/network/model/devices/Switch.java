package com.simulator.network.model.devices;

/**
 * Representa un dispositivo de red de tipo Switch.
 * Un switch es responsable de la conmutación de paquetes a nivel de enlace de datos
 * dentro de la simulación de red.
 * Esta clase concreta extiende {@link Device} y asigna automáticamente su tipo {@link DeviceType#SWITCH}.
 */
public class Switch extends Device {

    /**
     * Constructor por defecto requerido para la deserialización con Jackson.
     */
    public Switch() {
        super();
    }

    /**
     * Crea una nueva instancia de Switch con los datos proporcionados.
     *
     * @param id   Identificador único del dispositivo.
     * @param name Nombre descriptivo del dispositivo.
     */
    public Switch(String id, String name) {
        super(id, name, DeviceType.SWITCH);
    }
}
