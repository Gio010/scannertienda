package org.example.scannertienda.dao;

import org.example.scannertienda.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // =========================================================
    // MÉTODO AUXILIAR PRIVADO PARA EVITAR DUPLICACIÓN
    // =========================================================
    /**
     * Establece los parámetros comunes (1 a 6) para las sentencias INSERT y UPDATE.
     */
    private void setCommonParameters(PreparedStatement pstmt, Producto p) throws SQLException {
        pstmt.setString(1, p.getCodigo());
        pstmt.setString(2, p.getNombre());
        pstmt.setDouble(3, p.getPrecio());
        pstmt.setInt(4, p.getStock());
        pstmt.setString(5, p.getImagenPath());
        pstmt.setString(6, p.getCategoria());
    }

    // =========================================================
    // MÉTODOS CRUD (sin cambios)
    // =========================================================

    /**
     * Obtiene todos los productos de la base de datos para mostrarlos en la estantería y la tabla Admin.
     */
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio, stock, imagen_path, categoria FROM productos";

        try (Connection conn = ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getString("imagen_path"),
                        rs.getString("categoria")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ ERROR CRÍTICO al obtener todos los productos de la BD:");
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Busca un solo producto por su código de barras.
     */
    public Producto buscarProductoPorCodigo(String codigo) {
        Producto producto = null;
        String sql = "SELECT id, codigo, nombre, precio, stock, imagen_path, categoria FROM productos WHERE codigo = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = new Producto(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("imagen_path"),
                            rs.getString("categoria")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERROR al buscar producto por código:");
            e.printStackTrace();
        }

        return producto;
    }

    /**
     * Inserta un nuevo producto en la base de datos.
     */
    public void insertarProducto(Producto p) {
        String sql = "INSERT INTO productos (codigo, nombre, precio, stock, imagen_path, categoria) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setCommonParameters(pstmt, p);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
            if (e.getSQLState().startsWith("23")) {
                throw new RuntimeException("Error de Datos: El código de producto '" + p.getCodigo() + "' ya existe en el inventario. Use otro código.", e);
            }
            throw new RuntimeException("Error en la base de datos al guardar el producto.", e);
        }
    }

    /**
     * Actualiza los datos de un producto existente en la BD.
     */
    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, precio = ?, stock = ?, imagen_path = ?, categoria = ? WHERE id = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setCommonParameters(pstmt, p);

            pstmt.setInt(7, p.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            throw new RuntimeException("Error en la BD al actualizar el producto.", e);
        }
    }

    /**
     * Elimina un producto de la base de datos por su ID.
     */
    public void eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            throw new RuntimeException("Error en la BD al eliminar el producto.", e);
        }
    }

    // NUEVO MÓDULO DE PAGO: MÉTODO TRANSACCIONAL PARA STOCK

    /**
     * Actualiza el stock de un producto restándole la cantidad vendida.
     * 🚨 Importante: Utiliza la conexión transaccional proporcionada (conn).
     * @param conn La conexión JDBC de la transacción en curso.
     * @param idProducto ID del producto a actualizar.
     * @param cantidadVendida Cantidad a restar del stock actual.
     */
    public void decrementarStockTransaccional(Connection conn, int idProducto, int cantidadVendida) throws SQLException {
        // La consulta SQL resta la cantidad vendida al stock actual (stock = stock - ?)
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";

        // Usa la conexión 'conn' proporcionada, NO abre una nueva.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidadVendida);
            pstmt.setInt(2, idProducto);

            pstmt.executeUpdate();
        }
        // La excepción SQLException se lanza para que VentaDAO pueda hacer el ROLLBACK.
    }

    // El método que usabas antes (mantenido por si se usa en otra parte del Admin)
    public void actualizarStock(int idProducto, int cantidadVendida) {
        // La consulta SQL resta la cantidad vendida al stock actual (stock = stock - ?)
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cantidadVendida);
            pstmt.setInt(2, idProducto);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                System.err.println("Advertencia: No se encontró el producto con ID " + idProducto + " para actualizar el stock.");
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar el stock del producto: " + e.getMessage());
            throw new RuntimeException("Error en la BD al actualizar el stock. Venta cancelada.", e);
        }
    }
}