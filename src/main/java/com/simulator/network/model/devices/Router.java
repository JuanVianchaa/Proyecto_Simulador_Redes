package com.simulator.network.model.devices;

/**
 * Representa un dispositivo de red de tipo Router.
 * Un router es responsable de encaminar paquetes entre distintas redes dentro de la simulación.
 * Esta clase concreta extiende {@link Device} y configura automáticamente su tipo {@link DeviceType#ROUTER}.
 */
public class Router extends Device {

    /**
     * Constructor por defecto requerido para la deserialización con Jackson.
     */
    public Router() {
        super();
    }

    /**
     * Crea una nueva instancia de Router con los datos proporcionados.
     *
     * @param id   Identificador único del dispositivo.
     * @param name Nombre descriptivo del dispositivo.
     */
    public Router(String id, String name) {
        super(id, name, DeviceType.ROUTER);
    }
}
