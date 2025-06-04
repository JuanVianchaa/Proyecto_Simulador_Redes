package com.simulator.network.model.devices;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Representa la clase base abstracta para cualquier dispositivo de red dentro de la simulación.
 * Esta clase proporciona propiedades reactivas para el nombre y el tipo, facilita la integración con JavaFX,
 * y admite la serialización/deserialización polimórfica usando Jackson para persistencia en JSON.
 *
 * Las subclases deben representar tipos concretos de dispositivos, como PC, Router o Switch.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "_deviceType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PC.class, name = "PC"),
        @JsonSubTypes.Type(value = Router.class, name = "Router"),
        @JsonSubTypes.Type(value = Switch.class, name = "Switch")
})
public abstract class Device {

    private String id;
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<DeviceType> type = new SimpleObjectProperty<>();

    /**
     * Constructor por defecto requerido para la deserialización con Jackson.
     */
    protected Device() {}

    /**
     * Construye un dispositivo con los datos especificados.
     *
     * @param id    Identificador único del dispositivo (no puede ser nulo ni vacío).
     * @param name  Nombre descriptivo del dispositivo. Si es nulo o vacío, se usará el id.
     * @param type  Tipo de dispositivo a representar.
     * @throws IllegalArgumentException si el id es nulo o vacío.
     */
    protected Device(String id, String name, DeviceType type) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Device ID no puede ser null ni vacío.");
        }
        this.id = id;
        this.type.set(type);
        this.name.set((name == null || name.isBlank()) ? id : name);
    }

    /**
     * Retorna el identificador único del dispositivo.
     * @return id del dispositivo.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador único del dispositivo.
     * Usado principalmente por Jackson durante la deserialización.
     * @param id Nuevo identificador.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Devuelve la propiedad reactiva del nombre, útil para data binding en JavaFX.
     * @return propiedad StringProperty del nombre.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Obtiene el nombre descriptivo del dispositivo.
     * @return nombre del dispositivo.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Establece un nuevo nombre descriptivo para el dispositivo.
     * @param newName Nuevo nombre. No puede ser nulo ni vacío.
     * @throws IllegalArgumentException si el nuevo nombre es nulo o vacío.
     */
    public void setName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Nombre no puede ser null ni vacío.");
        }
        name.set(newName);
    }

    /**
     * Devuelve la propiedad reactiva del tipo de dispositivo, útil para data binding en JavaFX.
     * @return propiedad ObjectProperty del tipo.
     */
    public ObjectProperty<DeviceType> typeProperty() {
        return type;
    }

    /**
     * Obtiene el tipo actual del dispositivo.
     * @return DeviceType del dispositivo.
     */
    public DeviceType getType() {
        return type.get();
    }

    /**
     * Establece el tipo del dispositivo.
     * @param newType Nuevo tipo a asignar.
     */
    public void setType(DeviceType newType) {
        this.type.set(newType);
    }

    /**
     * Compara este dispositivo con otro basado en el id.
     * @param o Otro objeto.
     * @return true si los ids son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device device)) return false;
        return Objects.equals(id, device.id);
    }

    /**
     * Calcula el código hash basado en el id del dispositivo.
     * @return valor hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
