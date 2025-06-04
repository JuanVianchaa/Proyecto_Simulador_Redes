package com.simulator.network.model.graph;

/**
 * Interfaz que debe implementar cualquier clase que desee ser notificada
 * cuando ocurra un cambio en la topología de la red (nodos o enlaces) gestionada por {@link NetworkGraph}.
 *
 * Por ejemplo, la vista ({@code GraphView}) puede implementar esta interfaz para refrescar
 * automáticamente su contenido cuando el modelo cambia.
 */
public interface NetworkChangeListener {

    /**
     * Método invocado cuando la topología de la red ha cambiado, ya sea por la adición
     * o eliminación de dispositivos o enlaces.
     *
     * Las implementaciones típicamente deben actualizar la interfaz de usuario o el estado visual
     * para reflejar la nueva estructura de la red.
     */
    void onTopologyChanged();
}
