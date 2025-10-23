package org.example.scannertienda;

import org.example.scannertienda.dao.ProductoDAO;
import org.example.scannertienda.model.Producto;

import java.util.List;

public class TestProductos {
    public static void main(String[] args) {

        // 🚨 CORRECCIÓN: Crear una instancia para llamar al método no-estático
        List<Producto> productos = new ProductoDAO().obtenerTodos();

        for (Producto p : productos) {
            System.out.println("🛒 " + p.getNombre() + " - Q" + p.getPrecio());
        }
    }
}