package org.example.scannertienda.dao;

import org.example.scannertienda.model.Venta;
import org.example.scannertienda.model.ItemCarrito;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    // 1. Instancia del DAO de detalles para poder guardar los ítems.
    private DetalleVentaDAO detalleVentaDAO = new DetalleVentaDAO();
    private ProductoDAO productoDAO = new ProductoDAO(); // 🚨 AÑADIDO: Instancia de ProductoDAO

    /**
     * MÉTODO TRANSACCIONAL CLAVE
     * Inserta la venta principal y sus detalles en una sola transacción.
     * Incluye la actualización del stock. Si algo falla, toda la transacción se revierte (ROLLBACK).
     * @param venta El objeto Venta principal (total, idTrabajador).
     * @param items La lista de ItemCarrito con los productos de la venta.
     * @return El ID de la venta recién insertada (o lanza RuntimeException si falla).
     */
    public int insertarVenta(Venta venta, List<ItemCarrito> items) {
        String sqlVenta = "INSERT INTO ventas (total, id_trabajador) VALUES (?, ?)";
        int idVentaGenerada = -1;

        Connection conn = null;

        try {
            conn = ConexionBD.conectar();
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }

            //INICIO DE LA TRANSACCIÓN
            conn.setAutoCommit(false); // Deshabilitar auto-commit

            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {

                // 2. Insertar la Venta principal
                pstmtVenta.setDouble(1, venta.getTotal());
                pstmtVenta.setInt(2, venta.getIdTrabajador());

                int filasAfectadas = pstmtVenta.executeUpdate();

                if (filasAfectadas > 0) {
                    try (ResultSet rs = pstmtVenta.getGeneratedKeys()) {
                        if (rs.next()) {
                            idVentaGenerada = rs.getInt(1);
                        }
                    }
                } else {
                    throw new SQLException("Fallo al insertar la venta principal, no se generó ID.");
                }

                // 3. Insertar los Detalles de la Venta y Actualizar Stock
                for (ItemCarrito item : items) {
                    // 3.1 Insertar en detalle_venta
                    detalleVentaDAO.insertarDetalle(conn, idVentaGenerada, item);

                    // 🚨 AÑADIDO: Disminuir el stock usando la misma conexión (conn)
                    productoDAO.decrementarStockTransaccional(conn, item.getProducto().getId(), item.getCantidad());
                }

                // 4. Si todo salió bien (venta, detalles y stock), confirmamos los cambios
                conn.commit();

                return idVentaGenerada;

            } catch (SQLException e) {
                // Si algo falla, deshacemos todos los cambios
                if (conn != null) {
                    conn.rollback();
                    System.err.println("❌ ROLLBACK exitoso. Transacción cancelada por error: " + e.getMessage());
                }
                // Relanzamos la excepción para que el controlador la maneje
                throw new RuntimeException("Error en la BD al registrar la venta. Transacción cancelada.", e);
            } finally {
                // 5. Devolver la conexión a su estado normal
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener la conexión o al registrar la venta: " + e.getMessage());
            throw new RuntimeException("Error fatal de conexión al registrar la venta.", e);
        }
    }

    /**
     * Obtiene todas las ventas de la base de datos.
     */
    public List<Venta> obtenerTodasLasVentas() {
        // ... (El resto del método se mantiene igual)
        List<Venta> lista = new ArrayList<>();

        String sql = "SELECT id, total, fecha, id_trabajador FROM ventas ORDER BY fecha DESC";

        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha");
                LocalDateTime fechaVenta = ts != null ? ts.toLocalDateTime() : null;

                Venta venta = new Venta(
                        rs.getInt("id"),
                        rs.getDouble("total"),
                        fechaVenta,
                        rs.getInt("id_trabajador")
                );
                lista.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las ventas de la BD: " + e.getMessage());
            throw new RuntimeException("Error al cargar el reporte de ventas.", e);
        }
        return lista;
    }
}