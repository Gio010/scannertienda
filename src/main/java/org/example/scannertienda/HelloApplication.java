package org.example.scannertienda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        //Cargar la ventana de selección de roles
        //la ruta relativa al mismo paquete para simplificar la búsqueda.
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/scannertienda/inicio-roles-view.fxml"));

        try {
            // El tamaño se ajusta para la ventana de selección de roles (600x400)
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            stage.setTitle("AutomaticTienda S.A");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ ERROR FATAL: No se pudo cargar 'inicio-roles-view.fxml'.");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        // En JavaFX moderno, launch(args) es la forma estándar
        launch(args);
    }
}