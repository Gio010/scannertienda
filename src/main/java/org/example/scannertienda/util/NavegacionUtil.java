package org.example.scannertienda.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase de utilidad para manejar la navegación entre escenas comunes (ej. Volver a la pantalla de Roles).
 */
public class NavegacionUtil {

    public static void volverAInicio(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                NavegacionUtil.class.getResource("/org/example/scannertienda/inicio-roles-view.fxml")
        );
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("ScannerTienda - Selección de Rol");
        stage.setScene(new Scene(root, 600, 400)); // Tamaño fijo para la vista de roles
        stage.show();
    }

    public static void volverAAdmin(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                NavegacionUtil.class.getResource("/org/example/scannertienda/admin-view.fxml")
        );
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("ScannerTienda - Administración");
        // Usamos el tamaño estándar de la vista de administrador
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }
}

