package org.example.scannertienda;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int stock;
    private String imagenPath;

    public Producto(int id, String nombre, double precio, int stock, String imagenPath) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.imagenPath = imagenPath;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getImagenPath() { return imagenPath; }
}