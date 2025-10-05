package org.example.scannertienda;

import java.util.List;

public class TestProductos {
    public static void main(String[] args) {
        List<Producto> productos = ProductoDAO.obtenerProductos();

        for (Producto p : productos) {
            System.out.println("🛒 " + p.getNombre() + " - Q" + p.getPrecio());
        }
    }
}