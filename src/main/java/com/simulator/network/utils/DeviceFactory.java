package com.simulator.network.utils;

import com.simulator.network.model.devices.*;

/**
 * Clase utilitaria que implementa el patrón Factory Method para crear instancias concretas de {@link Device}
 * según el tipo especificado. Facilita la construcción centralizada y homogénea de dispositivos de red.
 */
public class DeviceFactory {

    /**
     * Crea una nueva instancia de {@link Device} del tipo especificado.
     *
     * @param type Tipo de dispositivo ({@link DeviceType#PC}, {@link DeviceType#ROUTER}, {@link DeviceType#SWITCH}, etc.).
     * @param id   Identificador único para el dispositivo.
     * @param name Nombre descriptivo; si es null o vacío, se usará el valor de id.
     * @return Instancia concreta de {@link Device} correspondiente al tipo indicado.
     * @throws IllegalArgumentException Si el tipo de dispositivo es null o no está soportado.
     */
    public static Device create(DeviceType type, String id, String name) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo de dispositivo no puede ser null.");
        }
        switch (type) {
            case PC:
                return new PC(id, name);
            case ROUTER:
                return new Router(id, name);
            case SWITCH:
                return new Switch(id, name);
            default:
                throw new IllegalArgumentException("Tipo de dispositivo no soportado: " + type);
        }
    }
}
