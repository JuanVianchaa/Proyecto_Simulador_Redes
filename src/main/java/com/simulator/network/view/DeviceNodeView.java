package com.simulator.network.view;

import com.simulator.network.model.devices.Device;
import com.simulator.network.model.devices.DeviceType;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Vista gráfica de un dispositivo de red, representada como un nodo circular con etiqueta.
 * Extiende {@link Group} e incluye:
 * <ul>
 *     <li>Un círculo coloreado según el tipo de dispositivo.</li>
 *     <li>Un texto centrado debajo del círculo que muestra el nombre del dispositivo.</li>
 * </ul>
 * Soporta comportamiento de arrastre para reubicar el nodo sobre la escena.
 */
public class DeviceNodeView extends Group {

    private final Device model;
    private final Circle circle;
    private final Text label;

    /**
     * Radio fijo del nodo circular que representa el dispositivo.
     */
    private static final double RADIUS = 20;

    private double dragOffsetX;
    private double dragOffsetY;

    /**
     * Crea una nueva vista gráfica de un dispositivo en la posición inicial indicada.
     *
     * @param model    Dispositivo asociado a este nodo.
     * @param initialX Posición inicial X del centro del nodo.
     * @param initialY Posición inicial Y del centro del nodo.
     */
    public DeviceNodeView(Device model, double initialX, double initialY) {
        this.model = model;

        circle = new Circle(0, 0, RADIUS);
        circle.setStroke(Color.BLACK);
        circle.setFill(determineColorByType(model.getType()));

        label = new Text(model.getName());
        label.setFont(Font.font(12));
        getChildren().addAll(circle, label);

        Tooltip.install(this, new Tooltip(model.getName()));

        setLayoutX(initialX);
        setLayoutY(initialY);

        label.applyCss();
        label.autosize();

        double textWidth = label.getLayoutBounds().getWidth();
        label.setX(-textWidth / 2);
        label.setY(RADIUS + 15);

        enableDrag();
    }

    /**
     * Determina el color del nodo según el tipo de dispositivo.
     *
     * @param type Tipo de dispositivo.
     * @return Color asociado al tipo.
     */
    private Color determineColorByType(DeviceType type) {
        return switch (type) {
            case PC -> Color.LIGHTBLUE;
            case ROUTER -> Color.ORANGE;
            case SWITCH -> Color.LIGHTGREEN;
            default -> Color.GRAY;
        };
    }

    /**
     * Habilita el comportamiento de arrastrar y soltar (drag & drop) para reposicionar el nodo.
     * Ajusta las propiedades de layoutX y layoutY.
     */
    private void enableDrag() {
        setOnMousePressed((MouseEvent event) -> {
            dragOffsetX = getLayoutX() - event.getSceneX();
            dragOffsetY = getLayoutY() - event.getSceneY();
            getScene().setCursor(Cursor.MOVE);
        });

        setOnMouseReleased(event -> {
            getScene().setCursor(Cursor.HAND);
        });

        setOnMouseDragged((MouseEvent event) -> {
            double newX = event.getSceneX() + dragOffsetX;
            double newY = event.getSceneY() + dragOffsetY;
            setLayoutX(newX);
            setLayoutY(newY);
        });

        setOnMouseEntered(event -> {
            if (!event.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
        });

        setOnMouseExited(event -> {
            if (!event.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    /**
     * Devuelve la propiedad reactiva de la posición X (centro) del nodo.
     * Puede ser usada por otros elementos para enlazar visualmente la posición.
     *
     * @return DoubleProperty de layoutX.
     */
    public javafx.beans.property.DoubleProperty centerXProperty() {
        return layoutXProperty();
    }

    /**
     * Devuelve la propiedad reactiva de la posición Y (centro) del nodo.
     * Puede ser usada por otros elementos para enlazar visualmente la posición.
     *
     * @return DoubleProperty de layoutY.
     */
    public javafx.beans.property.DoubleProperty centerYProperty() {
        return layoutYProperty();
    }
}
