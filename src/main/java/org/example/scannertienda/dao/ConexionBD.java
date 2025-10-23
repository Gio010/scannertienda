package org.example.scannertienda.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // ¡VERIFICA ESTOS VALORES!
    private static final String URL = "jdbc:postgresql://localhost:5432/supermercado_db";
    private static final String USUARIO = "postgres";
    private static final String CLAVE = "8090"; // <-- Si esta es incorrecta fallara aquí.

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexion exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            //Imprimir el stack trace para ver el error real
            System.err.println("ERROR CRITICO DE CONEXION A BASE DE DATOS:");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}