package org.example.scannertienda.model;

public class DetalleVenta {
    private int idVenta;
    private int idProducto;
    private String nombreProducto;
    private double precioUnitario;
    private int cantidad;

    // Constructor usado para cargar desde la BD (Reportes)
    public DetalleVenta(int idVenta, int idProducto, String nombreProducto, double precioUnitario, int cantidad) {
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    // Getters para la TableView
    public int getIdVenta() { return idVenta; }
    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }

    // Campo calculado para la tabla de Reportes
    public double getSubtotalItem() { return precioUnitario * cantidad; }
}