package com.simulator.network.model.graph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.simulator.network.model.devices.Device;

/**
 * Representa un enlace dirigido entre dos dispositivos de red,
 * especificando el dispositivo de origen, el de destino y la latencia asociada.
 * Esta clase es compatible con la serialización y deserialización en JSON mediante Jackson.
 */
public class Link {

    private Device source;
    private Device target;
    private double latency;

    /**
     * Constructor por defecto requerido para la deserialización con Jackson.
     */
    public Link() {}

    /**
     * Crea un nuevo enlace dirigido entre dos dispositivos, con la latencia especificada.
     * Este constructor es utilizado por Jackson durante la deserialización.
     *
     * @param source  Dispositivo de origen del enlace.
     * @param target  Dispositivo de destino del enlace.
     * @param latency Latencia (peso) del enlace en milisegundos.
     */
    @JsonCreator
    public Link(
            @JsonProperty("source") Device source,
            @JsonProperty("target") Device target,
            @JsonProperty("latency") double latency
    ) {
        this.source = source;
        this.target = target;
        this.latency = latency;
    }

    /**
     * Obtiene el dispositivo de origen de este enlace.
     *
     * @return Dispositivo fuente.
     */
    public Device getSource() {
        return source;
    }

    /**
     * Establece el dispositivo de origen de este enlace.
     * Usado principalmente durante la deserialización con Jackson.
     *
     * @param source Dispositivo fuente a asignar.
     */
    public void setSource(Device source) {
        this.source = source;
    }

    /**
     * Obtiene el dispositivo de destino de este enlace.
     *
     * @return Dispositivo destino.
     */
    public Device getTarget() {
        return target;
    }

    /**
     * Establece el dispositivo de destino de este enlace.
     * Usado principalmente durante la deserialización con Jackson.
     *
     * @param target Dispositivo destino a asignar.
     */
    public void setTarget(Device target) {
        this.target = target;
    }

    /**
     * Obtiene la latencia (peso) asociada a este enlace, en milisegundos.
     *
     * @return Latencia del enlace.
     */
    public double getLatency() {
        return latency;
    }

    /**
     * Establece la latencia asociada a este enlace.
     * Usado principalmente durante la deserialización con Jackson.
     *
     * @param latency Latencia en milisegundos.
     */
    public void setLatency(double latency) {
        this.latency = latency;
    }
}
