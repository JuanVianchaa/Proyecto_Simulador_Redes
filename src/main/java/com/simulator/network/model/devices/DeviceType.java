package com.simulator.network.model.devices;

/**
 * Enumeración que representa los tipos de dispositivos de red soportados en la simulación.
 * Cada valor corresponde a una clase concreta que extiende {@link Device}.
 * <ul>
 *   <li>{@link #PC} - Computador personal o terminal.</li>
 *   <li>{@link #ROUTER} - Dispositivo de encaminamiento/interconexión de redes.</li>
 *   <li>{@link #SWITCH} - Conmutador o dispositivo de interconexión a nivel de enlace.</li>
 * </ul>
 */
public enum DeviceType {
    /**
     * Computador personal o terminal.
     */
    PC,
    /**
     * Dispositivo de encaminamiento/interconexión de redes.
     */
    ROUTER,
    /**
     * Conmutador o dispositivo de interconexión a nivel de enlace.
     */
    SWITCH
}
