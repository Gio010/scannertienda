package org.example.scannertienda;

import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Importamos la interfaz Initializable
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

// Implementar Initializable es necesario para que JavaFX llame a initialize(URL, ResourceBundle)
public class EstanteriaController implements Initializable {

    // Componente inyectado desde estanteria.fxml
    @FXML
    private Accordion acordeonCategorias;

    /**
     * Este método se llama automáticamente después de que el FXML
     * y los elementos inyectados (@FXML) hayan sido cargados.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // --- Bloque TRY-CATCH para capturar el error de ejecución (Error 1) ---
        // Esto ayudará a atrapar fallos en la conexión a la base de datos (ProductoDAO)
        try {
            // **Punto crítico:** Si ProductoDAO falla (ej. SQLException), el error aparecerá aquí.
            List<Producto> productos = ProductoDAO.obtenerProductos();

            if (productos == null) {
                System.err.println("ERROR: La lista de productos es NULL. Revisar ProductoDAO.");
                return;
            }

            // Agrupar por categoría
            TitledPane jugos = crearSeccion("Jugos", productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains("jugo"))
                    .toList());

            TitledPane lacteos = crearSeccion("Lácteos", productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains("leche"))
                    .toList());

            TitledPane panaderia = crearSeccion("Panadería", productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains("pan"))
                    .toList());

            acordeonCategorias.getPanes().addAll(jugos, lacteos, panaderia);

        } catch (Exception e) {
            // Si hay un error, lo imprimimos completo para poder diagnosticarlo.
            System.err.println("--- FALLO CRÍTICO DE INICIALIZACIÓN ---");
            e.printStackTrace();
            // Opcionalmente, mostrar un error al usuario en la ventana:
            acordeonCategorias.getPanes().add(new TitledPane("Error", new Label("Error al cargar datos: " + e.getMessage())));
        }
    }

    // --- ESTE MÉTODO DEBE ESTAR FUERA DE initialize() ---
    private TitledPane crearSeccion(String titulo, List<Producto> productos) {
        TilePane estante = new TilePane();
        estante.setHgap(15);
        estante.setVgap(15);

        for (Producto p : productos) {
            VBox tarjeta = new VBox(5);
            tarjeta.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
            tarjeta.setPrefSize(120, 150);

            ImageView imagen;
            try {
                // Posible fallo si la ruta no existe, por eso está en un try-catch
                imagen = new ImageView(new Image("file:" + p.getImagenPath()));
            } catch (Exception e) {
                // Usar una imagen de placeholder si falla
                System.err.println("No se pudo cargar la imagen para: " + p.getNombre() + ". Usando placeholder.");
                // **NOTA:** Reemplaza "ruta/a/placeholder.png" con una imagen real en tu resources si es posible
                imagen = new ImageView();
                imagen.setFitWidth(100);
                imagen.setFitHeight(100);
            }

            imagen.setFitWidth(100);
            imagen.setFitHeight(100);

            Label nombre = new Label(p.getNombre());
            Label precio = new Label("Q" + p.getPrecio());

            tarjeta.getChildren().addAll(imagen, nombre, precio);
            estante.getChildren().add(tarjeta);
        }

        return new TitledPane(titulo, estante);
    }
}