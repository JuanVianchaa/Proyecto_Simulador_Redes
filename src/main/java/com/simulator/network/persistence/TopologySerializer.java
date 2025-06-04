package com.simulator.network.persistence;

import com.simulator.network.model.graph.NetworkGraph;

import java.io.File;
import java.io.IOException;

/**
 * Interfaz que define los métodos para la serialización y deserialización
 * de una topología de red ({@link NetworkGraph}) desde y hacia un archivo.
 * <p>
 * Permite implementar distintos formatos de persistencia, como JSON, XML, etc.
 */
public interface TopologySerializer {

    /**
     * Carga una instancia de {@link NetworkGraph} desde el archivo especificado.
     *
     * @param f Archivo de entrada que contiene la topología serializada.
     * @return Instancia deserializada de {@link NetworkGraph}.
     * @throws IOException Si ocurre un error de lectura o deserialización.
     */
    NetworkGraph load(File f) throws IOException;

    /**
     * Guarda la instancia de {@link NetworkGraph} en el archivo especificado.
     *
     * @param graph Instancia de la topología a serializar.
     * @param f     Archivo de destino para guardar la topología.
     * @throws IOException Si ocurre un error de escritura.
     */
    void save(NetworkGraph graph, File f) throws IOException;
}
