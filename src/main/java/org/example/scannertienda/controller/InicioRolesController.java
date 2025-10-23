package org.example.scannertienda.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class InicioRolesController {

    @FXML private Button btnConsumidor;
    @FXML private Button btnTrabajador;

    // Métodos utilitarios para mostrar alertas, etc., si los tienes...
    // (Asegúrate de tener un método para manejar errores si es necesario)

    // --- Módulo: Abrir Vista de Cliente/Consumidor ---
    public void abrirConsumidor(ActionEvent event) {
        try {
            // ❌ ERROR ANTERIOR: "estanteria-view.fxml"
            // ✅ CORRECCIÓN: "estanteria.fxml"
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/scannertienda/estanteria.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ScannerTienda - Estantería");
            stage.setScene(new Scene(root, 1200, 700)); // Tamaño de la estantería
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de estantería. Verifica el nombre del FXML.");
            e.printStackTrace();
        }
    }

    // --- Módulo CLAVE: Abrir Vista de Trabajador (Ahora carga el Login) ---
    public void abrirTrabajador(ActionEvent event) {
        try {
            // 🚨 CAMBIO CLAVE: Cargamos la vista de Login en lugar de Admin
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/scannertienda/login-trabajador-view.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ScannerTienda - Login");
            // Ajustamos el tamaño a la ventana de Login (600x400)
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login para Trabajadores.");
            e.printStackTrace();
            // Opcional: mostrar alerta al usuario
        }
    }
}