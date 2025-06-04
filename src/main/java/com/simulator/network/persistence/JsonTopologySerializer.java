package com.simulator.network.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulator.network.model.graph.NetworkGraph;

import java.io.File;
import java.io.IOException;

/**
 * Implementación de {@link TopologySerializer} que utiliza el formato JSON
 * para guardar y cargar la topología de la red mediante la librería Jackson.
 * <p>
 * Configura el {@link ObjectMapper} para ser tolerante con propiedades desconocidas
 * y con enums sin distinción de mayúsculas/minúsculas.
 */
public class JsonTopologySerializer implements TopologySerializer {

    private final ObjectMapper mapper;

    /**
     * Construye un serializador JSON con configuraciones de tolerancia
     * para facilitar la interoperabilidad y evitar errores por propiedades adicionales.
     */
    public JsonTopologySerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    /**
     * Carga una topología de red desde un archivo JSON especificado.
     *
     * @param file Archivo JSON desde el que se leerá la topología.
     * @return Instancia de {@link NetworkGraph} deserializada.
     * @throws IOException Si ocurre un error de lectura o deserialización.
     */
    @Override
    public NetworkGraph load(File file) throws IOException {
        return mapper.readValue(file, NetworkGraph.class);
    }

    /**
     * Guarda la topología de red especificada en un archivo en formato JSON legible.
     *
     * @param graph Instancia de {@link NetworkGraph} a serializar.
     * @param file  Archivo destino para guardar el JSON.
     * @throws IOException Si ocurre un error de escritura.
     */
    @Override
    public void save(NetworkGraph graph, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, graph);
    }
}
