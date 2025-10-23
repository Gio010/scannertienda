package org.example.scannertienda.model;

import java.time.LocalDateTime;

public class Venta {

    private int id;
    private double total;
    private LocalDateTime fecha;
    private int idTrabajador; // Clave foránea

    // Constructor completo (usado al cargar desde la BD)
    public Venta(int id, double total, LocalDateTime fecha, int idTrabajador) {
        this.id = id;
        this.total = total;
        this.fecha = fecha;
        this.idTrabajador = idTrabajador;
    }

    // Constructor para insertar nueva venta (usado en CarritoController)
    public Venta(double total, int idTrabajador) {
        this.total = total;
        this.idTrabajador = idTrabajador;
        // La BD debería establecer la fecha automáticamente,
        // pero la inicializamos por si acaso.
        this.fecha = LocalDateTime.now();
    }

    // Getters para la TableView
    public int getId() { return id; }
    public double getTotal() { return total; }
    public LocalDateTime getFecha() { return fecha; }
    public int getIdTrabajador() { return idTrabajador; }

    // Setters (opcionales, pero buena práctica si el modelo cambia)
    public void setId(int id) { this.id = id; }
    public void setTotal(double total) { this.total = total; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setIdTrabajador(int idTrabajador) { this.idTrabajador = idTrabajador; }
}