package com.simulator.network.model.simulation;

/**
 * Interfaz funcional utilizada como callback para notificar cuándo finaliza
 * la animación de un paquete durante la simulación de la red.
 *
 * Permite que los controladores o vistas reaccionen al evento de finalización
 * de la animación de un paquete.
 */
public interface SimulationListener {

    /**
     * Método invocado cuando la animación del paquete ha terminado.
     * Las implementaciones pueden utilizar este método para desencadenar
     * acciones posteriores en la simulación o interfaz.
     */
    void onSimulationFinished();
}
