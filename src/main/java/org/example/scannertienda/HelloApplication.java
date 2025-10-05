package org.example.scannertienda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // MODIFICADO: Se utiliza la ruta absoluta para asegurar que Maven encuentre el recurso.
        // La barra '/' indica que la búsqueda comienza en la carpeta 'resources'.
        // La ruta completa en el classpath es /org/example/scannertienda/estanteria.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/scannertienda/estanteria.fxml"));

        // **Verificación añadida**: Asegura que el recurso fue encontrado antes de cargarlo
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Error: No se encontró el recurso FXML en la ruta '/org/example/scannertienda/estanteria.fxml'.");
        }

        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Tamaño más amplio para la estantería
        stage.setTitle("🛒 Estantería - Scanner Tienda");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}