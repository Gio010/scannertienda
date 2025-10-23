package org.example.scannertienda.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

//importaciones de Modelos y DAOs (Usando subpaquetes)
import org.example.scannertienda.model.Producto;
import org.example.scannertienda.model.ItemCarrito;
import org.example.scannertienda.model.Venta;
import org.example.scannertienda.model.Usuario;
import org.example.scannertienda.dao.ProductoDAO;
import org.example.scannertienda.dao.VentaDAO;

public class CarritoController implements Initializable {

    // Componentes del Carrito
    @FXML private TableView<ItemCarrito> tablaCarrito;
    @FXML private TableColumn<ItemCarrito, String> colNombre;
    @FXML private TableColumn<ItemCarrito, Integer> colCantidad;
    @FXML private TableColumn<ItemCarrito, Double> colSubtotalItem;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnPagar;
    @FXML private Button btnCancelar;

    private EstanteriaController estanteriaController;
    private ObservableList<ItemCarrito> carrito = FXCollections.observableArrayList();
    private ProductoDAO productoDAO = new ProductoDAO();
    private VentaDAO ventaDAO = new VentaDAO();
    private Usuario trabajadorLogueado;

    /**
     * Permite al EstanteriaController inyectar su propia referencia en este CarritoController.
     */
    public void setEstanteriaController(EstanteriaController controller) {
        this.estanteriaController = controller;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarCarrito();
        this.trabajadorLogueado = new org.example.scannertienda.model.Usuario(1, "cliente_anonimo", "N/A", "Cajero Genérico");
    }

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    public void agregarProducto(org.example.scannertienda.model.Producto producto) {
        if (producto.getStock() <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto Agotado", "El producto " + producto.getNombre() + " no tiene stock disponible.");
            return;
        }

        ItemCarrito itemExistente = carrito.stream()
                .filter(item -> item.getProducto().getId() == producto.getId())
                .findFirst()
                .orElse(null);

        if (itemExistente != null) {
            if (itemExistente.getCantidad() + 1 > producto.getStock()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock Insuficiente", "No puedes agregar más de " + producto.getStock() + " unidades de " + producto.getNombre() + ".");
                return;
            }
            itemExistente.incrementarCantidad(1);
        } else {
            if (1 > producto.getStock()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock Insuficiente", "No puedes agregar más de " + producto.getStock() + " unidades de " + producto.getNombre() + ".");
                return;
            }
            carrito.add(new ItemCarrito(producto));
        }

        tablaCarrito.refresh();
        calcularTotales();
    }

    // --- MANEJO DE VISTA Y NAVEGACIÓN ---

    @FXML
    public void abrirVentanaPago(ActionEvent event) {
        if (carrito.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Carrito Vacío", "No hay productos en el carrito para pagar.");
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/scannertienda/pago-view.fxml"));
            Parent root = fxmlLoader.load();

            PagoController pagoController = fxmlLoader.getController();

            double total = Double.parseDouble(lblTotal.getText().replace("Q", ""));

            // Pasar los 4 argumentos (this, carrito, total, trabajadorLogueado)
            pagoController.setCarritoData(this, carrito, total, trabajadorLogueado);

            Stage stage = new Stage();
            stage.setTitle("Finalizar Pago");
            stage.setScene(new Scene(root, 600, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la ventana de pago.");
            e.printStackTrace();
        }
    }

    /**
     * Lógica final de la venta, ejecutada por PagoController.
     * @param nuevaVenta El objeto Venta a registrar.
     * @param itemsCarrito La lista de items a registrar.
     * @return El ID de la venta generada (o -1 si falló).
     */
    public int finalizarVenta(org.example.scannertienda.model.Venta nuevaVenta, List<ItemCarrito> itemsCarrito) {
        try {
            int idVentaGenerada = ventaDAO.insertarVenta(nuevaVenta, itemsCarrito);

            if (idVentaGenerada > 0) {
                // La limpieza del carrito y la recarga de stock deben hacerse aquí para que el PagoController pueda
                // usar el idVentaGenerada y el NIT/CF antes de que se borre el carrito.
                carrito.clear();
                calcularTotales();

                if (estanteriaController != null) {
                    estanteriaController.cargarEstanteria();
                }
            }

            return idVentaGenerada;

        } catch (RuntimeException e) {
            throw e;
        }
    }

    // ... (El resto de métodos auxiliares es el mismo)

    @FXML
    public void cancelarVenta(ActionEvent event) {
        carrito.clear();
        calcularTotales();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Venta Cancelada", "El carrito ha sido vaciado.");
    }

    private void inicializarCarrito() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotalItem.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaCarrito.setItems(carrito);
    }

    private void calcularTotales() {
        double total = carrito.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();

        lblSubtotal.setText(String.format("Q%.2f", total));
        lblTotal.setText(String.format("Q%.2f", total));
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public int obtenerCantidadEnCarrito(int idProducto) {
        return carrito.stream()
                .filter(item -> item.getProducto().getId() == idProducto)
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
    }
}