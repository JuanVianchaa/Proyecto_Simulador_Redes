package com.simulator.network.controller;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.devices.DeviceType;
import com.simulator.network.model.graph.Link;
import com.simulator.network.model.graph.NetworkGraph;
import com.simulator.network.persistence.JsonTopologySerializer;
import com.simulator.network.persistence.TopologySerializer;
import com.simulator.network.utils.DeviceFactory;
import com.simulator.network.view.GraphView;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Controlador encargado de gestionar todas las operaciones sobre la topología de red:
 * creación, modificación, eliminación de dispositivos y enlaces, guardado/carga y exportación a PDF.
 * Se comunica con el modelo de grafo, la vista y el sistema de persistencia.
 */
public class NetworkController {

    private final NetworkGraph modelGraph;
    private final GraphView graphView;
    private final TopologySerializer jsonSerializer;

    /**
     * Crea una nueva instancia del controlador de red.
     *
     * @param modelGraph Grafo de red que representa el modelo de datos.
     * @param graphView  Vista gráfica asociada para notificaciones y renderizado.
     */
    public NetworkController(NetworkGraph modelGraph, GraphView graphView) {
        this.modelGraph = modelGraph;
        this.graphView = graphView;
        this.jsonSerializer = new JsonTopologySerializer();
    }

    /**
     * Elimina todos los dispositivos y enlaces, dejando la topología vacía.
     * La vista se actualizará automáticamente por la suscripción como listener.
     */
    public void newTopology() {
        Set<Device> todos = Set.copyOf(modelGraph.getAllDevices());
        for (Device d : todos) {
            modelGraph.removeDevice(d);
        }
    }

    /**
     * Agrega un nuevo dispositivo al grafo.
     *
     * @param type Tipo de dispositivo a agregar (PC, ROUTER, SWITCH, etc.).
     * @param id   Identificador único del dispositivo.
     * @param name Nombre descriptivo del dispositivo. Si es null o vacío, se usará el id.
     * @throws IllegalArgumentException Si ya existe un dispositivo con ese id.
     */
    public void addDevice(DeviceType type, String id, String name) {
        if (existsDeviceWithId(id)) {
            throw new IllegalArgumentException("Ya existe un dispositivo con id = " + id);
        }
        Device nuevo = DeviceFactory.create(type, id, name);
        modelGraph.addDevice(nuevo);
    }

    /**
     * Elimina un dispositivo y sus enlaces asociados.
     *
     * @param id Identificador del dispositivo a eliminar.
     * @throws IllegalArgumentException Si el dispositivo no existe.
     */
    public void removeDevice(String id) {
        Device toRemove = findDeviceById(id);
        if (toRemove == null) {
            throw new IllegalArgumentException("No existe ningún dispositivo con id = " + id);
        }
        modelGraph.removeDevice(toRemove);
    }

    /**
     * Cambia el nombre descriptivo de un dispositivo.
     *
     * @param id      Identificador del dispositivo.
     * @param newName Nuevo nombre a asignar.
     * @throws IllegalArgumentException Si no se encuentra el dispositivo o el nombre es inválido.
     */
    public void renameDevice(String id, String newName) {
        Device d = findDeviceById(id);
        if (d == null) {
            throw new IllegalArgumentException("No existe ningún dispositivo con id = " + id);
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("El nuevo nombre no puede ser vacío.");
        }
        d.setName(newName);
        graphView.onTopologyChanged();
    }

    /**
     * Cambia el tipo de un dispositivo existente, manteniendo sus enlaces.
     *
     * @param id      Identificador del dispositivo.
     * @param newType Nuevo tipo a asignar.
     * @throws IllegalArgumentException Si no se encuentra el dispositivo o el tipo es nulo.
     */
    public void changeDeviceType(String id, DeviceType newType) {
        Device d = findDeviceById(id);
        if (d == null) {
            throw new IllegalArgumentException("No existe ningún dispositivo con id = " + id);
        }
        if (newType == null) {
            throw new IllegalArgumentException("DeviceType no puede ser null.");
        }
        d.setType(newType);
        graphView.onTopologyChanged();
    }

    /**
     * Conecta dos dispositivos de forma bidireccional con la latencia indicada.
     *
     * @param sourceId Identificador del primer dispositivo.
     * @param targetId Identificador del segundo dispositivo.
     * @param latency  Latencia en milisegundos (debe ser >= 0).
     * @throws IllegalArgumentException Si alguno de los dispositivos no existe o la latencia es negativa.
     */
    public void connectDevices(String sourceId, String targetId, double latency) {
        Device a = findDeviceById(sourceId);
        Device b = findDeviceById(targetId);
        if (a == null || b == null) {
            throw new IllegalArgumentException("Uno o ambos dispositivos no existen en el grafo.");
        }
        if (latency < 0) {
            throw new IllegalArgumentException("La latencia no puede ser negativa.");
        }
        modelGraph.connect(a, b, latency);
        modelGraph.connect(b, a, latency);
    }

    /**
     * Desconecta (elimina) el enlace entre dos dispositivos de forma bidireccional.
     *
     * @param sourceId Identificador del primer dispositivo.
     * @param targetId Identificador del segundo dispositivo.
     * @throws IllegalArgumentException Si alguno de los dispositivos no existe.
     */
    public void disconnectDevices(String sourceId, String targetId) {
        Device a = findDeviceById(sourceId);
        Device b = findDeviceById(targetId);
        if (a == null || b == null) {
            throw new IllegalArgumentException("Uno o ambos dispositivos no existen en el grafo.");
        }
        modelGraph.disconnect(a, b);
        modelGraph.disconnect(b, a);
    }

    /**
     * Muestra un diálogo para guardar la topología actual en un archivo JSON.
     *
     * @param ownerWindow Ventana principal de la aplicación.
     */
    public void promptSaveTopology(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar topología (JSON)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON (*.json)", "*.json")
        );
        File file = chooser.showSaveDialog(ownerWindow);
        if (file != null) {
            saveTopology(file);
        }
    }

    /**
     * Guarda la topología actual en el archivo JSON especificado.
     *
     * @param file Archivo destino con extensión .json.
     * @throws IllegalArgumentException Si la extensión no es .json.
     * @throws RuntimeException         Si ocurre un error de I/O.
     */
    public void saveTopology(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".json")) {
            throw new IllegalArgumentException("Formato no compatible: debe usarse .json");
        }
        try {
            jsonSerializer.save(modelGraph, file);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la topología: " + e.getMessage(), e);
        }
    }

    /**
     * Muestra un diálogo para abrir un archivo JSON y cargar la topología.
     *
     * @param ownerWindow Ventana principal de la aplicación.
     */
    public void promptLoadTopology(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir topología (JSON)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON (*.json)", "*.json")
        );
        File file = chooser.showOpenDialog(ownerWindow);
        if (file != null) {
            loadTopology(file);
        }
    }

    /**
     * Carga la topología desde el archivo JSON indicado y reemplaza la actual.
     *
     * @param file Archivo .json con la topología.
     * @throws IllegalArgumentException Si la extensión no es .json.
     * @throws RuntimeException         Si hay error en la lectura o deserialización.
     */
    public void loadTopology(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".json")) {
            throw new IllegalArgumentException("Formato no compatible: debe ser .json");
        }

        NetworkGraph loadedGraph;
        try {
            loadedGraph = jsonSerializer.load(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la topología: " + e.getMessage(), e);
        }

        Set<Device> actuales = Set.copyOf(modelGraph.getAllDevices());
        for (Device d : actuales) {
            modelGraph.removeDevice(d);
        }
        for (Device d : loadedGraph.getAllDevices()) {
            modelGraph.addDevice(d);
        }
        for (Link link : loadedGraph.getAllLinks()) {
            modelGraph.connect(link.getSource(), link.getTarget(), link.getLatency());
        }
    }

    /**
     * Muestra un diálogo para exportar la visualización del grafo a un archivo PDF.
     *
     * @param ownerWindow Ventana principal de la aplicación.
     */
    public void promptExportGraphAsPdf(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar grafo a PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf")
        );
        File file = chooser.showSaveDialog(ownerWindow);
        if (file != null) {
            try {
                exportGraphAsPdf(file);
            } catch (IOException e) {
                throw new RuntimeException("Error al exportar el grafo a PDF: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Exporta el contenido de la vista del grafo a un archivo PDF.
     *
     * @param file Archivo .pdf destino.
     * @throws IOException Si ocurre un error durante la escritura o el proceso de PDFBox.
     */
    private void exportGraphAsPdf(File file) throws IOException {
        SnapshotParameters params = new SnapshotParameters();
        WritableImage fxImage = graphView.snapshot(params, null);

        BufferedImage awtImage = SwingFXUtils.fromFXImage(fxImage, null);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(awtImage.getWidth(), awtImage.getHeight()));
        document.addPage(page);

        PDImageXObject pdImage = LosslessFactory.createFromImage(document, awtImage);
        var contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
        contentStream.drawImage(pdImage, 0, 0, awtImage.getWidth(), awtImage.getHeight());
        contentStream.close();

        document.save(file);
        document.close();
    }

    /**
     * Verifica si existe un dispositivo con el identificador dado.
     *
     * @param id Identificador a comprobar.
     * @return true si el dispositivo existe, false en caso contrario.
     */
    private boolean existsDeviceWithId(String id) {
        if (id == null) {
            return false;
        }
        return modelGraph.getAllDevices().stream()
                .anyMatch(d -> d.getId().equalsIgnoreCase(id));
    }

    /**
     * Busca un dispositivo por su identificador.
     *
     * @param id Identificador del dispositivo.
     * @return El dispositivo si lo encuentra, o null si no existe.
     */
    private Device findDeviceById(String id) {
        if (id == null) {
            return null;
        }
        for (Device d : modelGraph.getAllDevices()) {
            if (d.getId().equalsIgnoreCase(id)) {
                return d;
            }
        }
        return null;
    }
}
