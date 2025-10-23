package org.example.scannertienda.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Producto {

    // --- CAMPOS DE LA BASE DE DATOS ---
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty codigo;
    private final SimpleStringProperty nombre;
    private final SimpleDoubleProperty precio;
    private SimpleIntegerProperty stock; // El stock puede cambiar

    // 🚨 CAMPO NUEVO: Categoria
    private final SimpleStringProperty categoria;

    // Campo auxiliar para la ruta de la imagen
    private final SimpleStringProperty imagenPath;

    // Campo auxiliar para el carrito (cantidad)
    private SimpleIntegerProperty cantidad;

    // =========================================================
    // 🚨 CONSTRUCTOR PRINCIPAL (AHORA CON 7 ARGUMENTOS)
    // =========================================================
    public Producto(int id, String codigo, String nombre, double precio, int stock, String imagenPath, String categoria) {
        // Usamos Simple...Property para que la tabla de JavaFX pueda observar y actualizar los valores
        this.id = new SimpleIntegerProperty(id);
        this.codigo = new SimpleStringProperty(codigo);
        this.nombre = new SimpleStringProperty(nombre);
        this.precio = new SimpleDoubleProperty(precio);
        this.stock = new SimpleIntegerProperty(stock);
        this.imagenPath = new SimpleStringProperty(imagenPath);

        // 🚨 Inicialización del nuevo campo
        this.categoria = new SimpleStringProperty(categoria);

        this.cantidad = new SimpleIntegerProperty(1);
    }

    // =========================================================
    // GETTERS REQUERIDOS POR JavaFX TableView
    // =========================================================

    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getCodigo() { return codigo.get(); }
    public SimpleStringProperty codigoProperty() { return codigo; }

    public String getNombre() { return nombre.get(); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    public double getPrecio() { return precio.get(); }
    public SimpleDoubleProperty precioProperty() { return precio; }

    public int getStock() { return stock.get(); }
    public SimpleIntegerProperty stockProperty() { return stock; }

    public void setStock(int nuevoStock) {
        this.stock.set(nuevoStock);
    }

    public String getImagenPath() { return imagenPath.get(); }
    public SimpleStringProperty imagenPathProperty() { return imagenPath; }

    // 🚨 NUEVOS GETTERS PARA CATEGORÍA
    public String getCategoria() { return categoria.get(); }
    public SimpleStringProperty categoriaProperty() { return categoria; }

    // --- Getters y Setters para el Carrito ---
    public int getCantidad() { return cantidad.get(); }
    public void setCantidad(int cantidad) { this.cantidad.set(cantidad); }
}