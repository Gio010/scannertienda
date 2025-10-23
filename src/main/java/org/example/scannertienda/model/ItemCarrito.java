package org.example.scannertienda.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ItemCarrito {

    private final Producto producto;
    private final SimpleStringProperty nombre;
    private final SimpleIntegerProperty cantidad;
    private final SimpleDoubleProperty precioUnitario;
    private final SimpleDoubleProperty subtotal;

    public ItemCarrito(Producto producto) {
        this.producto = producto;
        this.nombre = new SimpleStringProperty(producto.getNombre());
        this.cantidad = new SimpleIntegerProperty(1);
        this.precioUnitario = new SimpleDoubleProperty(producto.getPrecio());
        this.subtotal = new SimpleDoubleProperty(producto.getPrecio());
    }

    // --- Getters para la Tabla (TableView) ---
    public String getNombre() { return nombre.get(); }
    public int getCantidad() { return cantidad.get(); }
    public double getSubtotal() { return subtotal.get(); }

    // --- Métodos de Lógica ---
    public Producto getProducto() { return producto; }

    public void incrementarCantidad(int num) {
        setCantidad(cantidad.get() + num);
    }

    public void setCantidad(int nuevaCantidad) {
        if (nuevaCantidad >= 0) {
            cantidad.set(nuevaCantidad);
            recalcularSubtotal();
        }
    }

    private void recalcularSubtotal() {
        double nuevoSubtotal = (double) cantidad.get() * precioUnitario.get();
        subtotal.set(nuevoSubtotal);
    }
}