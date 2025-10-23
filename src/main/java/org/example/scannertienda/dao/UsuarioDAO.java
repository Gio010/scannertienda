package org.example.scannertienda.dao;

import org.example.scannertienda.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Verifica si un usuario y contraseña coinciden en la base de datos.
     * @return El objeto Usuario si las credenciales son correctas, o null si fallan.
     */
    public Usuario verificarCredenciales(String user, String pass) {
        // Consulta SQL para buscar al usuario y verificar la contraseña
        String sql = "SELECT id, usuario, rol FROM usuarios WHERE usuario = ? AND password_simple = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, pass);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Credenciales correctas. Retornamos un objeto Usuario
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            pass, // La contraseña no se almacena, pero la pasamos para completar el objeto
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la BD durante la verificación de credenciales: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Credenciales inválidas o error de BD
    }
}
