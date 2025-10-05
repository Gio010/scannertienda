package org.example.scannertienda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public static List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, stock, imagen_path FROM productos";

        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getString("imagen_path")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR CRÍTICO al obtener productos de la BD:");
            e.printStackTrace();
        }

        return lista;
    }
}