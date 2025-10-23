package org.example.scannertienda;

import org.example.scannertienda.dao.ConexionBD;

public class TestConexion {
    public static void main(String[] args) {
        ConexionBD.conectar();
    }
}