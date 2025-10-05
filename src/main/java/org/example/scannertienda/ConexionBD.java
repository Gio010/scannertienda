package org.example.scannertienda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // ¡VERIFICA ESTOS VALORES!
    private static final String URL = "jdbc:postgresql://localhost:5432/supermercado_db";
    private static final String USUARIO = "postgres";
    private static final String CLAVE = "8090"; // <-- Si esta es incorrecta, fallará aquí.

    public static Connection conectar() {
        try {
            // Se asume que el driver JDBC de PostgreSQL se carga automáticamente
            Connection conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("✅ Conexión exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            // 🚨 CORRECCIÓN CRÍTICA: Imprimir el stack trace para ver el error real
            System.err.println("❌ ERROR CRÍTICO DE CONEXIÓN A BASE DE DATOS:");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace(); // <-- ¡ESTO ES LO NUEVO!
            return null;
        }
    }
}