package org.example.scannertienda.dao;

import org.example.scannertienda.model.DetalleVenta;
import org.example.scannertienda.model.ItemCarrito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAO {

    /**
     * Inserta los detalles de un solo item de la venta usando una conexión externa (para la transacción).
     * @param conn La conexión activa de la transacción.
     * @param idVenta El ID de la venta (factura) a la que pertenece el detalle.
     * @param item El ItemCarrito con la información del producto, cantidad y precio.
     */
    public void insertarDetalle(Connection conn, int idVenta, ItemCarrito item) throws SQLException {
        //Agregar la columna 'subtotal' a la lista de columnas
        String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        // Agregar un placeholder (?) para el subtotal

        // La conexión se recibe como parámetro.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);
            pstmt.setInt(2, item.getProducto().getId());
            pstmt.setInt(3, item.getCantidad());
            pstmt.setDouble(4, item.getProducto().getPrecio());

            //Asignar el valor de item.getSubtotal() al 5to placeholder
            pstmt.setDouble(5, item.getSubtotal());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Obtiene todos los detalles productos vendidos
     * @param idVenta El ID de la venta a consultar.
     */
    public List<DetalleVenta> obtenerDetallesPorVenta(int idVenta) {
        List<DetalleVenta> detalles = new ArrayList<>();

        //Cambiar dv.id_producto a dv.producto_id
        String sql = "SELECT dv.venta_id, dv.producto_id, p.nombre, dv.precio_unitario, dv.cantidad " +
                "FROM detalle_venta dv " +
                // 🚨 CORRECCIÓN CLAVE 2: Cambiar dv.id_producto a dv.producto_id en el JOIN
                "JOIN productos p ON dv.producto_id = p.id " +
                "WHERE dv.venta_id = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenta);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta detalle = new DetalleVenta(
                            rs.getInt("venta_id"),
                            // 🚨 CORRECCIÓN CLAVE 3: Cambiar "id_producto" a "producto_id"
                            rs.getInt("producto_id"),
                            // El nombre viene de la tabla 'productos' (alias p)
                            rs.getString("nombre"),
                            rs.getDouble("precio_unitario"),
                            rs.getInt("cantidad")
                    );
                    detalles.add(detalle);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta para ID " + idVenta + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cargar los detalles de la venta.", e);
        }
        return detalles;
    }
}