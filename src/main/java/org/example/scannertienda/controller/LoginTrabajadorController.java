package org.example.scannertienda.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.scannertienda.dao.UsuarioDAO;
import org.example.scannertienda.model.Usuario;
import org.example.scannertienda.util.NavegacionUtil;

import java.io.IOException;

public class LoginTrabajadorController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // --- Módulo: Iniciar Sesión ---
    public void iniciarSesion(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        Usuario user = usuarioDAO.verificarCredenciales(usuario, password);

        if (user != null) {
            // Credenciales válidas: Cargar la vista de Administración
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Bienvenido, " + user.getUsuario() + ".");

            try {
                // Cargar Admin-View
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/scannertienda/admin-view.fxml"));
                Parent root = fxmlLoader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("ScannerTienda - Administración");
                // La vista de AdminController es más grande
                stage.setScene(new Scene(root, 1000, 700));
                stage.show();

            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la vista de administración.");
                e.printStackTrace();
            }

        } else {
            // Credenciales inválidas
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "Usuario o contraseña incorrectos.");
            txtPassword.clear(); // Limpiar la contraseña después del intento fallido
        }
    }

    // --- Módulo: Volver a Inicio (Reutilizando la utilidad) ---
    public void volverAInicio(ActionEvent event) {
        try {
            NavegacionUtil.volverAInicio(event);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo volver a la ventana de inicio.");
            e.printStackTrace();
        }
    }
}