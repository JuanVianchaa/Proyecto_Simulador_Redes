package com.simulator.network.view;

import com.simulator.network.controller.NetworkController;
import com.simulator.network.controller.SimulationController;
import com.simulator.network.model.devices.Device;
import com.simulator.network.model.devices.DeviceType;
import com.simulator.network.model.graph.NetworkGraph;
import com.simulator.network.model.simulation.BfsStrategy;
import com.simulator.network.model.simulation.DijkstraStrategy;
import com.simulator.network.model.simulation.RoutingStrategy;
import com.simulator.network.persistence.JsonTopologySerializer;
import com.simulator.network.utils.DeviceFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ventana principal de la aplicación y punto de entrada del simulador.
 * <p>
 * Proporciona una interfaz gráfica integral para:
 * <ul>
 *     <li>Gestión visual de la topología de red (dispositivos y enlaces)</li>
 *     <li>Simulación de envío de paquetes y benchmark de algoritmos de enrutamiento</li>
 *     <li>Edición de propiedades, guardado/carga de topologías y exportación a PDF</li>
 * </ul>
 * <p>
 * Organiza la UI en un menú superior, una barra lateral de herramientas,
 * un área central dividida entre el grafo y los paneles de propiedades de dispositivos/enlaces.
 */
public class MainWindow extends Application {

    private NetworkGraph modelGraph;
    private GraphView graphView;
    private NetworkController networkController;
    private SimulationController simulationController;

    /**
     * Listas observables para sincronizar tablas de dispositivos y enlaces.
     */
    private final ObservableList<Device> deviceList = FXCollections.observableArrayList();
    private final ObservableList<com.simulator.network.model.graph.Link> linkList = FXCollections.observableArrayList();

    /**
     * Inicializa y muestra la ventana principal del simulador.
     *
     * @param primaryStage Stage principal de la aplicación JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        // 1) Inicializar modelo y vista
        modelGraph = new NetworkGraph();
        graphView = new GraphView(modelGraph);

        // 2) Inicializar controladores
        networkController = new NetworkController(modelGraph, graphView);
        simulationController = new SimulationController(modelGraph, graphView);

        // 3) Construir layout principal
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar(primaryStage));

        // SplitPane horizontal: izquierda toolbar, centro+derecha grafo+propiedades
        SplitPane mainSplit = new SplitPane();
        mainSplit.setDividerPositions(0.15, 0.6);
        mainSplit.getItems().addAll(createToolBar(), createCenterPane());
        root.setCenter(mainSplit);

        // 4) Sincronizar listas inicialmente
        updateLists();

        // 5) Suscribirse a cambios de topología para refrescar tablas
        modelGraph.addListener(() -> Platform.runLater(this::updateLists));

        // 6) Configurar y mostrar escena
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulador de Redes de Computadoras");
        primaryStage.show();
    }

    /**
     * Crea la barra de menú superior con opciones para archivo, simulación, benchmarks y ayuda.
     */
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // --- Menú Archivo ---
        Menu archivoMenu = new Menu("Archivo");

        MenuItem nuevoItem = new MenuItem("Nuevo");
        nuevoItem.setOnAction(e -> {
            networkController.newTopology();
            updateLists();
        });

        MenuItem abrirItem = new MenuItem("Abrir...");
        abrirItem.setOnAction(e -> {
            networkController.promptLoadTopology(stage);
            updateLists();
        });

        MenuItem guardarItem = new MenuItem("Guardar...");
        guardarItem.setOnAction(e -> networkController.promptSaveTopology(stage));

        // <-- Volvemos a añadir aquí el ítem “Exportar a PDF” -->
        MenuItem exportarPdfItem = new MenuItem("Exportar a PDF");
        exportarPdfItem.setOnAction(e -> networkController.promptExportGraphAsPdf(stage));

        archivoMenu.getItems().addAll(
                nuevoItem,
                abrirItem,
                guardarItem,
                new SeparatorMenuItem(),
                exportarPdfItem,      // <–– este es el nuevo ítem
                new SeparatorMenuItem()
        );

        // --- Menú Simulación ---
        Menu simMenu = new Menu("Simulación");
        MenuItem simularItem = new MenuItem("Simular Paquete");
        simularItem.setOnAction(e -> promptSimulatePacket());
        Menu estrategiaSubMenu = new Menu("Estrategia de Ruteo");
        RadioMenuItem dijkstraItem = new RadioMenuItem("Dijkstra");
        RadioMenuItem bfsItem = new RadioMenuItem("BFS");
        ToggleGroup tg = new ToggleGroup();
        dijkstraItem.setToggleGroup(tg);
        bfsItem.setToggleGroup(tg);
        dijkstraItem.setSelected(true);
        dijkstraItem.setOnAction(ev -> simulationController.setRoutingStrategy(new DijkstraStrategy()));
        bfsItem.setOnAction(ev -> simulationController.setRoutingStrategy(new BfsStrategy()));
        estrategiaSubMenu.getItems().addAll(dijkstraItem, bfsItem);
        simMenu.getItems().addAll(simularItem, new SeparatorMenuItem(), estrategiaSubMenu);

        // --- Menú Benchmark ---
        Menu benchmarkMenu = new Menu("Benchmark");
        MenuItem runBenchmark = new MenuItem("Ruteo N nodos");
        runBenchmark.setOnAction(e -> promptBenchmark());
        benchmarkMenu.getItems().add(runBenchmark);

        MenuItem runBenchmarkJson = new MenuItem("Benchmark desde JSON");
        runBenchmarkJson.setOnAction(e -> promptBenchmarkFromJson(stage));
        benchmarkMenu.getItems().add(runBenchmarkJson);


        // --- Menú Ayuda ---
        Menu ayudaMenu = new Menu("Ayuda");
        MenuItem acercaItem = new MenuItem("Acerca de...");
        acercaItem.setOnAction(e -> showAboutDialog());
        ayudaMenu.getItems().add(acercaItem);

        menuBar.getMenus().addAll(archivoMenu, simMenu, benchmarkMenu, ayudaMenu);
        return menuBar;
    }

    /**
     * Lógica para benchmark desde un archivo JSON.
     */
    private void promptBenchmarkFromJson(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivo de topología (JSON)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos JSON", "*.json")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        // Intentar cargar el grafo y correr el benchmark
        try {
            JsonTopologySerializer serializer = new JsonTopologySerializer();
            NetworkGraph graph = serializer.load(file);
            Device origen = findDeviceById(graph, "1");
            Device destino = findDeviceById(graph, "200");
            if (origen == null || destino == null) {
                showAlert("Error", "No se encontraron nodos con ID \"1\" y/o \"200\" en la topología cargada.");
                return;
            }
            int reps = 5;
            StringBuilder report = new StringBuilder();
            report.append("Benchmark (topología desde archivo JSON)\n\n");

            // Dijkstra
            report.append("== Dijkstra ==\n");
            double avgDijkstra = measureStrategyWithJumps(graph, origen, destino, new DijkstraStrategy(), reps, report);

            // BFS
            report.append("\n== BFS ==\n");
            double avgBFS = measureStrategyWithJumps(graph, origen, destino, new BfsStrategy(), reps, report);

            // Resumen de cumplimiento
            report.append("\n\n--- Resultados ---\n");
            report.append("Dijkstra: ").append(avgDijkstra < 1000 ? "✔ Cumple (< 1 s)\n" : "✘ No cumple (>= 1 s)\n");
            report.append("BFS: ").append(avgBFS < 1000 ? "✔ Cumple (< 1 s)\n" : "✘ No cumple (>= 1 s)\n");

            // Mostrar
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Resultados Benchmark (JSON)");
            alert.setHeaderText("Benchmark finalizado");
            TextArea area = new TextArea(report.toString());
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefWidth(600);
            area.setPrefHeight(400);
            alert.getDialogPane().setContent(area);
            alert.showAndWait();

        } catch (IOException ex) {
            showAlert("Error al leer JSON", ex.getMessage());
        }
    }


    /**
     * Mide y reporta el tiempo de ejecución de una estrategia, mostrando además los saltos de la ruta.
     */
    private double measureStrategyWithJumps(NetworkGraph graph, Device origen, Device destino,
                                            RoutingStrategy strategy, int reps, StringBuilder report) {
        String name = strategy.getClass().getSimpleName();
        long totalNano = 0;
        for (int i = 1; i <= reps; i++) {
            long start = System.nanoTime();
            java.util.List<Device> camino = strategy.calculatePath(graph, origen, destino);
            long elapsed = System.nanoTime() - start;
            totalNano += elapsed;
            int saltos = (camino == null || camino.isEmpty()) ? 0 : camino.size() - 1;
            report.append(String.format("  Ejecución %d (%s): %.3f ms (Saltos=%d)%n",
                    i, name, elapsed / 1_000_000.0, saltos));
        }
        double avgMs = (totalNano / (double) reps) / 1_000_000.0;
        report.append(String.format("  Promedio %s: %.3f ms (%.3f s)%n", name, avgMs, avgMs / 1000.0));
        return avgMs;
    }


    /**
     * Crea la barra de herramientas lateral izquierda con botones para agregar dispositivos,
     * conectar nodos y refrescar la vista del grafo.
     */
    private VBox createToolBar() {
        VBox toolBar = new VBox(10);
        toolBar.setPadding(new Insets(10));
        toolBar.setStyle("-fx-background-color: #f0f0f0;");

        Button addPCBtn = new Button("Agregar PC");
        addPCBtn.setMaxWidth(Double.MAX_VALUE);
        addPCBtn.setOnAction(e -> promptAddDevice(DeviceType.PC));

        Button addRouterBtn = new Button("Agregar Router");
        addRouterBtn.setMaxWidth(Double.MAX_VALUE);
        addRouterBtn.setOnAction(e -> promptAddDevice(DeviceType.ROUTER));

        Button addSwitchBtn = new Button("Agregar Switch");
        addSwitchBtn.setMaxWidth(Double.MAX_VALUE);
        addSwitchBtn.setOnAction(e -> promptAddDevice(DeviceType.SWITCH));

        Button connectBtn = new Button("Conectar Nodos");
        connectBtn.setMaxWidth(Double.MAX_VALUE);
        connectBtn.setOnAction(e -> promptConnectNodes());

        Button refreshGraphBtn = new Button("Actualizar Grafo");
        refreshGraphBtn.setMaxWidth(Double.MAX_VALUE);
        refreshGraphBtn.setOnAction(e -> {
            graphView.onTopologyChanged();
            // Si quieres, puedes mostrar un mensaje:
            showAlert("Info", "¡Grafo actualizado!");
        });


        toolBar.getChildren().addAll(addPCBtn, addRouterBtn, addSwitchBtn, new Separator(), connectBtn, refreshGraphBtn);

        return toolBar;
    }

    /**
     * Crea el área central con la visualización del grafo y el panel de propiedades (tabs).
     */
    private SplitPane createCenterPane() {
        SplitPane centerSplit = new SplitPane();
        centerSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        centerSplit.setDividerPositions(0.7);

        BorderPane graphPane = new BorderPane();
        graphPane.setCenter(graphView);

        TabPane propertiesTabs = new TabPane();
        propertiesTabs.getTabs().addAll(createDevicesTab(), createLinksTab());

        centerSplit.getItems().addAll(graphPane, propertiesTabs);
        return centerSplit;
    }

    /**
     * Pestaña con la tabla de dispositivos y acciones de edición.
     */
    private Tab createDevicesTab() {
        Tab tab = new Tab("Dispositivos");
        tab.setClosable(false);

        TableView<Device> table = new TableView<>(deviceList);
        table.setEditable(true);

        TableColumn<Device, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        idCol.setPrefWidth(100);
        idCol.setEditable(false);

        TableColumn<Device, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            Device dev = event.getRowValue();
            String newName = event.getNewValue().trim();
            if (!newName.isEmpty()) {
                networkController.renameDevice(dev.getId(), newName);
                updateLists();
                graphView.onTopologyChanged();
            }
        });
        nameCol.setPrefWidth(200);

        TableColumn<Device, DeviceType> typeCol = new TableColumn<>("Tipo");
        typeCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getType()));
        typeCol.setCellFactory(ComboBoxTableCell.forTableColumn(DeviceType.values()));
        typeCol.setOnEditCommit(event -> {
            Device dev = event.getRowValue();
            DeviceType newType = event.getNewValue();
            String id = dev.getId();
            String name = dev.getName();
            networkController.removeDevice(id);
            networkController.addDevice(newType, id, name);
            updateLists();
        });
        typeCol.setPrefWidth(150);

        TableColumn<Device, Void> actionCol = new TableColumn<>("Acciones");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Eliminar");

            {
                deleteBtn.setOnAction(e -> {
                    Device dev = getTableView().getItems().get(getIndex());
                    networkController.removeDevice(dev.getId());
                    updateLists();
                });
                deleteBtn.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionCol.setPrefWidth(100);

        table.getColumns().setAll(idCol, nameCol, typeCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(table);
        return tab;
    }

    /**
     * Pestaña con la tabla de enlaces y acciones de edición.
     */
    private Tab createLinksTab() {
        Tab tab = new Tab("Enlaces");
        tab.setClosable(false);

        TableView<com.simulator.network.model.graph.Link> table = new TableView<>(linkList);
        table.setEditable(true);

        TableColumn<com.simulator.network.model.graph.Link, String> srcCol = new TableColumn<>("Origen");
        srcCol.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getSource().getId()));
        srcCol.setPrefWidth(100);
        srcCol.setEditable(false);

        TableColumn<com.simulator.network.model.graph.Link, String> tgtCol = new TableColumn<>("Destino");
        tgtCol.setCellValueFactory(l -> new SimpleStringProperty(l.getValue().getTarget().getId()));
        tgtCol.setPrefWidth(100);
        tgtCol.setEditable(false);

        TableColumn<com.simulator.network.model.graph.Link, Number> latencyCol = new TableColumn<>("Latencia (ms)");
        latencyCol.setCellValueFactory(l -> new SimpleDoubleProperty(l.getValue().getLatency()));
        latencyCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.NumberStringConverter()));
        latencyCol.setOnEditCommit(event -> {
            var link = event.getRowValue();
            double newLat;
            try {
                newLat = Double.parseDouble(event.getNewValue().toString());
                if (newLat < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Latencia inválida. Debe ser un número >= 0.");
                updateLists();
                return;
            }
            String sId = link.getSource().getId();
            String tId = link.getTarget().getId();
            networkController.connectDevices(sId, tId, newLat);
            updateLists();
            graphView.onTopologyChanged();
        });
        latencyCol.setPrefWidth(150);

        TableColumn<com.simulator.network.model.graph.Link, Void> actionCol = new TableColumn<>("Acciones");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Eliminar");

            {
                deleteBtn.setOnAction(e -> {
                    var link = getTableView().getItems().get(getIndex());
                    networkController.disconnectDevices(link.getSource().getId(), link.getTarget().getId());
                    updateLists();
                });
                deleteBtn.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        actionCol.setPrefWidth(100);

        table.getColumns().setAll(srcCol, tgtCol, latencyCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        tab.setContent(table);
        return tab;
    }

    /**
     * Sincroniza las listas observables con el estado actual del modelo.
     */
    private void updateLists() {
        deviceList.setAll(modelGraph.getAllDevices());
        linkList.setAll(modelGraph.getAllLinks());
    }

    /**
     * Sincroniza las listas observables con el estado actual del modelo.
     */
    private void promptAddDevice(DeviceType type) {
        // Generar ID automático (el menor entero positivo no usado)
        Set<Integer> usedIds = new HashSet<>();
        for (Device d : modelGraph.getAllDevices()) {
            try {
                usedIds.add(Integer.parseInt(d.getId()));
            } catch (NumberFormatException ignored) {
            }
        }

        int nextId = 1;
        while (usedIds.contains(nextId)) nextId++;
        String generatedId = String.valueOf(nextId);

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Agregar " + type);
        nameDialog.setHeaderText("ID generado: " + generatedId);
        nameDialog.setContentText("Ingrese nombre (opcional):");
        nameDialog.showAndWait().ifPresent(name -> {
            try {
                String finalName = name.isBlank() ? generatedId : name.trim();
                networkController.addDevice(type, generatedId, finalName);
                updateLists();
            } catch (IllegalArgumentException ex) {
                showAlert("Error al agregar dispositivo", ex.getMessage());
            }
        });
    }

    /**
     * Diálogo para conectar dos nodos seleccionando origen, destino y latencia.
     */
    private void promptConnectNodes() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Conectar Nodos");
        dialog.setHeaderText("Seleccione Origen, Destino y Latencia:");

        // Mapear nombre → ID para buscar luego
        Map<String, String> nameToId = new HashMap<>();
        for (Device d : modelGraph.getAllDevices()) {
            nameToId.put(d.getName(), d.getId());
        }

        // ComboBoxes que muestran nombres en pantalla
        ComboBox<String> sourceCombo = new ComboBox<>();
        ComboBox<String> targetCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(nameToId.keySet());
        targetCombo.getItems().addAll(nameToId.keySet());
        sourceCombo.setPromptText("Nombre Origen");
        targetCombo.setPromptText("Nombre Destino");

        // Campo para latencia
        TextField latencyField = new TextField();
        latencyField.setPromptText("Latencia (ms)");

        VBox content = new VBox(10,
                new Label("Nombre Origen:"), sourceCombo,
                new Label("Nombre Destino:"), targetCombo,
                new Label("Latencia (ms):"), latencyField
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String srcName = sourceCombo.getValue();
                String tgtName = targetCombo.getValue();
                if (srcName == null || tgtName == null) {
                    showAlert("Error", "Debe seleccionar origen y destino.");
                    return null;
                }
                String srcId = nameToId.get(srcName);
                String tgtId = nameToId.get(tgtName);
                try {
                    double lat = Double.parseDouble(latencyField.getText().trim());
                    networkController.connectDevices(srcId, tgtId, lat);
                    updateLists();
                } catch (NumberFormatException ex) {
                    showAlert("Error", "La latencia debe ser un número válido.");
                } catch (IllegalArgumentException ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    /**
     * Diálogo para ejecutar la simulación de envío de paquete entre dos nodos.
     */
    private void promptSimulatePacket() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Simular Paquete");
        dialog.setHeaderText("Seleccione Origen y Destino:");

        // Mapear nombre → ID para buscar luego
        Map<String, String> nameToId = new HashMap<>();
        for (Device d : modelGraph.getAllDevices()) {
            nameToId.put(d.getName(), d.getId());
        }

        // ComboBoxes que muestran nombres en pantalla
        ComboBox<String> sourceCombo = new ComboBox<>();
        ComboBox<String> targetCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(nameToId.keySet());
        targetCombo.getItems().addAll(nameToId.keySet());
        sourceCombo.setPromptText("Nombre Origen");
        targetCombo.setPromptText("Nombre Destino");

        VBox content = new VBox(10,
                new Label("Nombre Origen:"), sourceCombo,
                new Label("Nombre Destino:"), targetCombo
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String srcName = sourceCombo.getValue();
                String tgtName = targetCombo.getValue();
                if (srcName == null || tgtName == null) {
                    showAlert("Error al simular", "Debe seleccionar Origen y Destino.");
                    return null;
                }

                // FORZAR siempre Dijkstra justo antes de simular:
                simulationController.setRoutingStrategy(new DijkstraStrategy());

                String srcId = nameToId.get(srcName);
                String tgtId = nameToId.get(tgtName);
                try {
                    simulationController.simulatePacket(srcId, tgtId);
                } catch (IllegalArgumentException ex) {
                    showAlert("Error al simular", ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    /**
     * Diálogo para ejecutar un benchmark de rutas en un grafo generado de N nodos.
     */
    private void promptBenchmark() {
        TextInputDialog dialog = new TextInputDialog("200");
        dialog.setTitle("Benchmark Ruteo");
        dialog.setHeaderText("Ingrese número de nodos (p.ej., 200):");
        dialog.setContentText("N nodos:");

        dialog.showAndWait().ifPresent(input -> {
            int n;
            try {
                n = Integer.parseInt(input.trim());
                if (n < 2) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert("Entrada inválida", "Ingrese un entero ≥ 2.");
                return;
            }
            runBenchmark(n);
        });
    }

    /**
     * Lógica para construir un grafo con N nodos y medir estrategias de ruteo.
     */
    private void runBenchmark(int n) {
        // Construir gráfica con n nodos
        NetworkGraph benchGraph = createGraph(n);
        Device origen = findDeviceById(benchGraph, "1");
        Device destino = findDeviceById(benchGraph, String.valueOf(n));
        if (origen == null || destino == null) {
            showAlert("Error", "No se encontraron nodos 1 o " + n);
            return;
        }

        final int reps = 5;
        StringBuilder report = new StringBuilder();
        report.append("Benchmark en ").append(n).append(" nodos:\n\n");

        // Dijkstra
        report.append("== Dijkstra ==\n");
        measureStrategy(benchGraph, origen, destino, new DijkstraStrategy(), reps, report);

        // BFS
        report.append("\n== BFS ==\n");
        measureStrategy(benchGraph, origen, destino, new BfsStrategy(), reps, report);

        // Mostrar resultado
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultados Benchmark");
        alert.setHeaderText("Benchmark finalizado");
        TextArea area = new TextArea(report.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(500);
        area.setPrefHeight(400);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    /**
     * Mide el tiempo de ejecución de una estrategia de enrutamiento.
     */
    private void measureStrategy(NetworkGraph graph, Device origen, Device destino,
                                 RoutingStrategy strategy, int reps, StringBuilder report) {
        String name = strategy.getClass().getSimpleName();
        long totalNano = 0;
        int lastHops = 0;
        for (int i = 1; i <= reps; i++) {
            long start = System.nanoTime();
            java.util.List<Device> path = strategy.calculatePath(graph, origen, destino);
            long elapsed = System.nanoTime() - start;
            totalNano += elapsed;
            int hops = (path == null || path.size() < 2) ? 0 : (path.size() - 1);
            lastHops = hops; // Para mostrarlo luego del ciclo
            report.append(String.format("  Ejecución %d (%s): %.3f ms (Saltos=%d)%n",
                    i, name, elapsed / 1_000_000.0, hops));
        }
        double avgMs = (totalNano / (double) reps) / 1_000_000.0;
        report.append(String.format("  Promedio %s: %.3f ms (%.3f s), Saltos: %d%n", name, avgMs, avgMs / 1000.0, lastHops));
    }

    /**
     * Utilidad para crear un grafo de prueba con N nodos y enlaces consecutivos.
     */
    private NetworkGraph createGraph(int n) {
        NetworkGraph g = new NetworkGraph();
        String[] types = {"PC", "Router", "Switch"};
        DeviceFactory factory = new DeviceFactory();

        for (int i = 1; i <= n; i++) {
            String id = String.valueOf(i);
            String t = types[(i - 1) % types.length];
            DeviceType dt = DeviceType.valueOf(t.toUpperCase());
            Device d = DeviceFactory.create(dt, id, "Device-" + id);
            g.addDevice(d);
        }

        for (int i = 1; i < n; i++) {
            Device a = findDeviceById(g, String.valueOf(i));
            Device b = findDeviceById(g, String.valueOf(i + 1));
            g.connect(a, b, 10.0);
            g.connect(b, a, 10.0);
        }

        return g;
    }

    /**
     * Busca un dispositivo por ID dentro de un grafo dado.
     */
    private Device findDeviceById(NetworkGraph graph, String id) {
        for (Device d : graph.getAllDevices()) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Utilidad para mostrar un diálogo de alerta.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Diálogo "Acerca de" con información de la aplicación.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Simulador de Redes de Computadoras");
        alert.setContentText("Versión 1.0\nDesarrollado en JavaFX\n\n© 2025");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
