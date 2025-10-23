package org.example.scannertienda.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.example.scannertienda.dao.ProductoDAO;
import org.example.scannertienda.model.Producto;
import org.example.scannertienda.util.NavegacionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EstanteriaController implements Initializable {

    // 🚨 CONSTANTES (Rutas nuevas y viejas)
    private static final String CLASSPATH_IMG_BASE = "/org/example/scannertienda/img/";
    private static final String DATA_IMAGE_FOLDER_NAME = "data" + File.separator + "img";

    @FXML private VBox contenedorEstantes;
    @FXML private CarritoController carritoIncController; // Inyectado desde el FXML

    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 🚨 CAMBIO CLAVE: Inyectar la referencia de este controlador (Estanteria) en el Carrito
        // Esto permite que el CarritoController pueda llamar a cargarEstanteria() después de una venta.
        if (carritoIncController != null) {
            carritoIncController.setEstanteriaController(this);
        }
        cargarEstanteria();
    }

    // --- Módulo: Volver a Inicio ---
    public void volverAInicio(ActionEvent event) {
        try {
            NavegacionUtil.volverAInicio(event);

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo volver a la ventana de inicio.");
            e.printStackTrace();
        }
    }

    /**
     * Carga los productos de la BD, los agrupa por categoría y crea los estantes visuales.
     * ES PÚBLICO para ser llamado desde CarritoController.
     */
    public void cargarEstanteria() {
        if (contenedorEstantes == null) {
            System.err.println("❌ ERROR: El componente 'contenedorEstantes' es null. Falla la inyección FXML.");
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Interfaz", "La estantería no se cargó correctamente.");
            return;
        }

        try {
            // Esta línea es crucial: Vuelve a obtener el stock actualizado de la BD.
            List<Producto> productos = productoDAO.obtenerTodos();

            if (productos != null && !productos.isEmpty()) {
                // Esta línea es crucial: Limpia la vista antes de dibujar de nuevo.
                contenedorEstantes.getChildren().clear();

                // Agrupar y dibujar la estantería
                productos.stream()
                        .collect(Collectors.groupingBy(Producto::getCategoria))
                        .forEach((nombreCategoria, listaProductos) -> {
                            contenedorEstantes.getChildren().add(crearEstanteVisual(nombreCategoria, listaProductos));
                        });
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudieron cargar los productos de la base de datos.");
            e.printStackTrace();
        }
    }

    // 🚨 MÉTODO FALTANTE #1: Crea el contenedor visual de la categoría
    private VBox crearEstanteVisual(String tituloCategoria, List<Producto> productos) {
        VBox estanteContenedor = new VBox();
        estanteContenedor.setStyle("-fx-background-color: #8d6e63; -fx-border-color: #5d4037; -fx-border-width: 0 0 10 0;");

        // Título
        Label tituloLabel = new Label("Categoría: " + tituloCategoria.toUpperCase());
        tituloLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        tituloLabel.setPadding(new Insets(15, 0, 10, 15));

        // Repisa de productos (donde van las tarjetas)
        TilePane repisaProductos = new TilePane();
        repisaProductos.setHgap(20);
        repisaProductos.setVgap(20);
        repisaProductos.setPadding(new Insets(15, 15, 15, 15));
        repisaProductos.setAlignment(Pos.TOP_LEFT);
        repisaProductos.setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #999999; -fx-border-width: 0;");

        for (Producto p : productos) {
            VBox tarjeta = crearTarjetaProducto(p);
            repisaProductos.getChildren().add(tarjeta);
        }

        estanteContenedor.getChildren().addAll(tituloLabel, repisaProductos);
        return estanteContenedor;
    }


    // 🚨 MÉTODO FALTANTE #2: Crea la tarjeta visual de un producto
    private VBox crearTarjetaProducto(Producto p) {
        VBox tarjeta = new VBox(5);

        tarjeta.setStyle("-fx-background-color: white; " +
                "-fx-padding: 8; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-width: 1;");

        tarjeta.setPrefSize(130, 190);
        tarjeta.setAlignment(javafx.geometry.Pos.CENTER);
        tarjeta.setCursor(javafx.scene.Cursor.HAND);

        ImageView imagen = obtenerImagenProducto(p);

        Label nombre = new Label(p.getNombre());
        nombre.setWrapText(true);
        nombre.setAlignment(javafx.geometry.Pos.CENTER);

        Label precio = new Label("Q" + String.format("%.2f", p.getPrecio()));
        precio.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");

        // LÓGICA CLAVE: Click en la tarjeta agrega al carrito
        tarjeta.setOnMouseClicked(event -> {
            if (carritoIncController != null) {
                carritoIncController.agregarProducto(p);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El carrito no está disponible.");
            }
        });

        tarjeta.getChildren().addAll(imagen, nombre, precio);
        return tarjeta;
    }


    private ImageView obtenerImagenProducto(Producto p) {
        ImageView imagenView = new ImageView();
        Image imagenProducto = null;
        String imagenPath = p.getImagenPath();

        // 1. Intentar cargar la imagen real solo si la ruta existe
        if (imagenPath != null && !imagenPath.trim().isEmpty()) {

            try {
                if (imagenPath.startsWith(DATA_IMAGE_FOLDER_NAME)) {
                    // 🚨 CASO A: Es una ruta de archivo del sistema (Ej: data/img/coca.png)
                    File imageFile = new File(imagenPath);
                    if (imageFile.exists()) {
                        // Usar file: para cargar desde el sistema de archivos
                        imagenProducto = new Image(imageFile.toURI().toString(), 100, 100, true, true);
                    }
                } else if (imagenPath.startsWith(CLASSPATH_IMG_BASE)) {
                    // 🚨 CASO B: Es una ruta de recurso interno (productos antiguos)
                    imagenProducto = new Image(getClass().getResource(imagenPath).toExternalForm(), 100, 100, true, true);
                }

            } catch (Exception ignore) {
                // Capturar cualquier error si la ruta es inválida
                imagenProducto = null;
            }
        }

        // 2. Si la carga falló (imagenProducto es null o tiene error)
        if (imagenProducto == null || (imagenProducto != null && imagenProducto.isError())) {
            try {
                // 3. Cargar la imagen de PLACEHOLDER
                Image placeholder = new Image(getClass().getResource(CLASSPATH_IMG_BASE + "placeholder.png").toExternalForm(), 100, 100, true, true);
                imagenView.setImage(placeholder);
            } catch (Exception ex) {
                System.err.println("❌ ERROR: No se pudo encontrar la imagen de placeholder en " + CLASSPATH_IMG_BASE + "placeholder.png.");
            }
        } else {
            // 4. Si la imagen se cargó correctamente, usarla.
            imagenView.setImage(imagenProducto);
        }

        imagenView.setFitWidth(100);
        imagenView.setFitHeight(100);
        imagenView.setPreserveRatio(true);
        return imagenView;
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}