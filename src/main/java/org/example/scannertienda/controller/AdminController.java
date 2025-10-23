package org.example.scannertienda.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; //para cargar la nueva vista
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.example.scannertienda.dao.ProductoDAO;
import org.example.scannertienda.model.Producto;
import org.example.scannertienda.util.NavegacionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    //Conexiones FXML de la Tabla
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colID;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colRutaImagen;

    //Conexiones FXML del Formulario
    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtRutaImagen;

    //el ComboBox para la categoría
    @FXML private ComboBox<String> cmbCategoria;

    private ProductoDAO productoDAO;
    private String imagenSeleccionadaPath;
    private Producto productoSeleccionadoParaEdicion;
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    //Categorías Fijas
    private final String[] CATEGORIAS_VALIDAS = {"BEBIDAS", "SNACKS", "ABARROTES", "HIGIENE", "PANADERIA"};


    //RUTAS DE IMAGEN ACTUALIZADAS PARA ARCHIVOS DEL SISTEMA
    private static final String DATA_IMAGE_FOLDER = "data" + File.separator + "img";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productoDAO = new ProductoDAO();

        // 1. Configurar las Columnas de la Tabla
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colRutaImagen.setCellValueFactory(new PropertyValueFactory<>("imagenPath"));

        //Inicializar el ComboBox con las categorías fijas
        cmbCategoria.setItems(FXCollections.observableArrayList(CATEGORIAS_VALIDAS));

        //2.Listener para cargar datos al formulario al seleccionar una fila
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> mostrarDetallesProducto(newValue));

        //3.Cargar los datos iniciales
        cargarProductos();
    }

    // --- metodos de logica base
    private void cargarProductos() {
        productos.clear();
        List<Producto> listaBD = productoDAO.obtenerTodos();
        productos.addAll(listaBD);
        tablaProductos.setItems(productos);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarDetallesProducto(Producto producto) {
        if (producto != null) {
            productoSeleccionadoParaEdicion = producto;

            txtNombre.setText(producto.getNombre());
            txtCodigo.setText(producto.getCodigo());
            txtPrecio.setText(String.format("%.2f", producto.getPrecio()));
            txtStock.setText(String.valueOf(producto.getStock()));

            String dbPath = producto.getImagenPath();
            if (dbPath != null && !dbPath.isEmpty()) {
                // Muestra solo el nombre del archivo
                String separator = dbPath.contains(File.separator) ? File.separator : "/";
                txtRutaImagen.setText(dbPath.substring(dbPath.lastIndexOf(separator) + 1));
            } else {
                txtRutaImagen.clear();
            }

            this.imagenSeleccionadaPath = null;

            //Seleccionar la categoria del producto en el ComboBox
            cmbCategoria.getSelectionModel().select(producto.getCategoria());

        } else {
            limpiarFormulario();
        }
    }

    private void limpiarFormulario() {
        productoSeleccionadoParaEdicion = null;
        txtNombre.clear();
        txtCodigo.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtRutaImagen.clear();
        this.imagenSeleccionadaPath = null;
        // 🚨 CLAVE: Limpiar la selección de categoría
        cmbCategoria.getSelectionModel().clearSelection();
    }

    public void volverAInicio(ActionEvent event) {
        try {
            NavegacionUtil.volverAInicio(event);
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo volver a la ventana de inicio.");
            e.printStackTrace();
        }
    }

    //Seleccionar imagen y copiar a carpeta de datos
    @FXML
    public void seleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Producto");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            try {
                String originalFileName = selectedFile.getName();
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

                //Generar un nombre de archivo único o basado en el codigo
                String newFileName = txtCodigo.getText().trim().isEmpty()
                        ? "producto_" + System.currentTimeMillis() + extension
                        : txtCodigo.getText().trim().toLowerCase() + extension;

                //1. Definir y crear el directorio de destino(data/img)
                File targetDir = new File(DATA_IMAGE_FOLDER);
                if (!targetDir.exists()) {
                    targetDir.mkdirs(); // Crea 'data' y 'img' si no existen
                }

                File targetFile = new File(targetDir, newFileName);

                // 2. Copiar el archivo seleccionado a la carpeta DATA/IMG
                Files.copy(
                        selectedFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                //3. Almacenar la ruta relativa del sistema para la BD
                this.imagenSeleccionadaPath = DATA_IMAGE_FOLDER + File.separator + newFileName;

                //4. Mostrar el nombre de archivo al usuario
                txtRutaImagen.setText(newFileName);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Imagen copiada a la carpeta de datos: " + newFileName);

            } catch (IOException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Copiar", "No se pudo copiar la imagen al directorio de datos. Verifique permisos.");
                e.printStackTrace();
            }
        }
    }

    //Módulo Guardar
    @FXML
    public void guardarProducto(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String codigo = txtCodigo.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();
        // 🚨 CLAVE: Obtener la categoría del ComboBox
        String categoria = cmbCategoria.getSelectionModel().getSelectedItem();

        // 1.Validación inicial
        if (nombre.isEmpty() || codigo.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || categoria == null || categoria.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Todos los campos, incluyendo la Categoría, son obligatorios.");
            return;
        }

        // 2.determinar la ruta final de la imagen
        String pathFinal = "";
        if (imagenSeleccionadaPath != null) {
            pathFinal = imagenSeleccionadaPath;
        } else if (productoSeleccionadoParaEdicion != null) {
            pathFinal = productoSeleccionadoParaEdicion.getImagenPath();
        } else {
            // Caso C:nuevo producto sin imagen.
            mostrarAlerta(Alert.AlertType.WARNING, "Imagen Faltante", "Debe seleccionar una imagen para el nuevo producto.");
            return;
        }


        try {
            // 3.validar formato numerico
            double precio = Double.parseDouble(precioStr);
            int stock = Integer.parseInt(stockStr);

            if (precio <= 0 || stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Error de Datos", "Precio o Stock no válidos.");
                return;
            }

            // 4.logica de Edición vs. Inserción
            if (productoSeleccionadoParaEdicion != null) {
                //modo edicion
                //se pasa la categoría al constructor
                Producto productoEditado = new Producto(
                        productoSeleccionadoParaEdicion.getId(),
                        codigo,
                        nombre,
                        precio,
                        stock,
                        pathFinal,
                        categoria // <--- ¡AÑADIDO!
                );
                productoDAO.actualizarProducto(productoEditado);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto '" + nombre + "' actualizado correctamente.");

            } else {
                //modo insercion
                //se pasa la categoría al constructor
                Producto nuevoProducto = new Producto(0, codigo, nombre, precio, stock, pathFinal, categoria); // <--- ¡AÑADIDO!
                productoDAO.insertarProducto(nuevoProducto);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto '" + nombre + "' guardado y añadido al inventario.");
            }

            //5.Limpiar y refrescar
            cargarProductos();
            tablaProductos.getSelectionModel().clearSelection();
            mostrarDetallesProducto(null);

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "El Precio y el Stock deben ser números válidos.");
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Guardado", e.getMessage());
        }
    }

    @FXML
    public void eliminarProducto(ActionEvent event) {
        if (productoSeleccionadoParaEdicion == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Debe seleccionar un producto de la tabla para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("Eliminar Producto: " + productoSeleccionadoParaEdicion.getNombre());
        alert.setContentText("¿Está seguro de que desea eliminar permanentemente este producto del inventario?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productoDAO.eliminarProducto(productoSeleccionadoParaEdicion.getId());

                    cargarProductos();
                    mostrarDetallesProducto(null);

                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                } catch (RuntimeException e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Eliminación", e.getMessage());
                }
            }
        });
    }

    //Abrir la Vista de reportes
    @FXML
    public void abrirReporteDeVentas(ActionEvent event) {
        try {
            // 🚨 RUTA CLAVE: Este es el FXML que debemos crear.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/scannertienda/reportes-ventas-view.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("ScannerTienda - Reportes de Ventas");
            stage.setScene(new Scene(root, 800, 600)); // Tamaño sugerido
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de reportes de ventas: " + e.getMessage());
            e.printStackTrace();
            // Mostrar alerta al usuario sobre el error
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo abrir el Reporte de Ventas. " +
                    "Verifique que el archivo 'reportes-ventas-view.fxml' exista.");
        }
    }

}