package org.example.scannertienda.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// 🚨 IMPORTACIONES DE MODELOS NECESARIAS
import org.example.scannertienda.model.ItemCarrito;
import org.example.scannertienda.model.Venta;
import org.example.scannertienda.model.Usuario;

// 🚨 IMPORTACIONES DE PDF (iText 7)
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PagoController implements Initializable {

    // Componentes del FXML
    @FXML private TableView<ItemCarrito> tablaResumen;
    @FXML private TableColumn<ItemCarrito, String> colNombre;
    @FXML private TableColumn<ItemCarrito, Integer> colCantidad;
    @FXML private TableColumn<ItemCarrito, Double> colSubtotalItem;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TextField txtMontoRecibido;
    @FXML private Label lblTotal;
    @FXML private Label lblCambio;
    @FXML private Button btnGenerarFactura;

    // 🚨 NUEVOS COMPONENTES FXML PARA NIT/CF
    @FXML private ComboBox<String> cmbTipoCliente;
    @FXML private TextField txtNIT;
    @FXML private Label lblErrorNIT;

    // Referencia al controlador principal del carrito
    private CarritoController carritoController;
    // Lista de items
    private ObservableList<ItemCarrito> itemsCarrito = FXCollections.observableArrayList();
    private double totalVenta = 0.0;
    private Usuario trabajadorLogueado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Inicializar Tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colSubtotalItem.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaResumen.setItems(itemsCarrito);

        // 2. Inicializar ComboBox de Pago
        cmbMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta de Crédito", "Tarjeta de Debito"));
        cmbMetodoPago.setValue("Efectivo");

        // 3. Listener para cambios en el método de pago
        txtMontoRecibido.textProperty().addListener((obs, oldVal, newVal) -> calcularCambio(null));
        cmbMetodoPago.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Efectivo".equals(newVal)) {
                txtMontoRecibido.setDisable(false);
                txtMontoRecibido.requestFocus();
            } else {
                txtMontoRecibido.setText(String.format("%.2f", totalVenta)); // Monto exacto para tarjeta
                txtMontoRecibido.setDisable(true);
                lblCambio.setText("Q0.00");
                btnGenerarFactura.setDisable(false); // Siempre habilitado si no es efectivo
            }
            calcularCambio(null);
        });

        // 🚨 4. LÓGICA DE INICIALIZACIÓN Y VALIDACIÓN NIT/CF
        cmbTipoCliente.setItems(FXCollections.observableArrayList("CF (Consumidor Final)", "NIT"));
        cmbTipoCliente.setValue("CF (Consumidor Final)");
        txtNIT.setDisable(true);
        lblErrorNIT.setText(""); // Limpiar errores al inicio

        // Listener para habilitar/deshabilitar el campo NIT
        cmbTipoCliente.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean esNIT = "NIT".equals(newVal);
            txtNIT.setDisable(!esNIT);
            if (!esNIT) {
                txtNIT.setText("");
                lblErrorNIT.setText("");
            }
        });

        // Listener para validar que el NIT solo tenga 9 dígitos numéricos
        txtNIT.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!txtNIT.isDisable() && newVal != null) {
                // Validación: Solo números y máximo 9 dígitos
                if (newVal.matches("\\d*") && newVal.length() <= 9) {
                    lblErrorNIT.setText("");
                } else if (!newVal.matches("\\d*")) {
                    // Si ingresó algo no numérico, lo revertimos
                    txtNIT.setText(oldVal);
                    lblErrorNIT.setText("Solo se permiten dígitos numéricos.");
                } else if (newVal.length() > 9) {
                    // Si excede 9 dígitos, lo revertimos
                    txtNIT.setText(oldVal);
                }
            }
        });

        // 5. Forzar cálculo inicial
        calcularCambio(null);
    }

    // --- SETTER CLAVE para recibir la información del Carrito ---

    public void setCarritoData(CarritoController controller, ObservableList<ItemCarrito> items, double total, Usuario trabajador) {
        this.carritoController = controller;
        this.itemsCarrito.addAll(items); // Copiar los datos del carrito
        this.totalVenta = total;
        this.trabajadorLogueado = trabajador; // Guardamos el usuario

        lblTotal.setText(String.format("Q%.2f", totalVenta));

        calcularCambio(null);
    }

    // --- LÓGICA DE PAGO ---

    @FXML
    public void calcularCambio(javafx.event.Event event) {
        if (!"Efectivo".equals(cmbMetodoPago.getValue())) {
            lblCambio.setText("Q0.00");
            btnGenerarFactura.setDisable(false);
            return;
        }

        double montoRecibido = 0.0;
        try {
            montoRecibido = Double.parseDouble(txtMontoRecibido.getText());
        } catch (NumberFormatException e) {
            lblCambio.setText("Q0.00");
            btnGenerarFactura.setDisable(true);
            return;
        }

        double cambio = montoRecibido - totalVenta;

        if (cambio >= 0) {
            lblCambio.setText(String.format("Q%.2f", cambio));
            btnGenerarFactura.setDisable(false);
        } else {
            lblCambio.setText(String.format("Faltan Q%.2f", Math.abs(cambio)));
            btnGenerarFactura.setDisable(true);
        }
    }

    // --- MÉTODO CLAVE: FINALIZAR PAGO Y GENERAR PDF ---
    @FXML
    public void finalizarPago(ActionEvent event) {
        if (carritoController == null || trabajadorLogueado == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Sistema", "Falta información del trabajador o del carrito.");
            return;
        }

        // 1. Validaciones Finales
        if (btnGenerarFactura.isDisable()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Monto Insuficiente", "El monto recibido no cubre el total de la venta.");
            return;
        }

        // 🚨 2. Capturar y Validar el NIT/CF
        String nitCliente;
        String tipoSeleccionado = cmbTipoCliente.getValue();

        if ("NIT".equals(tipoSeleccionado)) {
            nitCliente = txtNIT.getText().trim();
            // Validación final: 9 dígitos exactos
            if (nitCliente.isEmpty() || !nitCliente.matches("\\d{9}")) {
                lblErrorNIT.setText("Debe ser un NIT válido de 9 dígitos.");
                mostrarAlerta(Alert.AlertType.ERROR, "Error de NIT", "Por favor, ingrese un NIT válido de 9 dígitos.");
                return; // Detiene el proceso
            }
        } else {
            nitCliente = "CF"; // Usar "CF" si es Consumidor Final
        }

        int idVentaGenerada = -1;

        try {
            // 3. Crear el objeto Venta (Total, ID Trabajador)
            Venta nuevaVenta = new Venta(totalVenta, trabajadorLogueado.getId());

            // 4. Ejecutar la lógica de venta transaccional y capturar el ID
            // **ADVERTENCIA**: DEBES ACTUALIZAR EL MÉTODO finalizarVenta en CarritoController para que reciba el objeto Venta.
            idVentaGenerada = carritoController.finalizarVenta(nuevaVenta, itemsCarrito);

            if (idVentaGenerada == -1) {
                throw new RuntimeException("El registro de la venta falló y se revirtió (ID=-1).");
            }

            // 5. 🚨 Generar Factura PDF usando el ID generado y el NIT/CF
            generarFacturaPDF(idVentaGenerada, nitCliente);

            // 6. Mensaje de éxito
            String metodo = cmbMetodoPago.getValue();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Venta Exitosa",
                    "Venta Finalizada con Éxito (Factura ID: " + idVentaGenerada + ").\n" +
                            "Pago: " + metodo + " por Q" + String.format("%.2f", totalVenta) + ".");

            // 7. Cerrar la ventana modal
            cerrarVentana(event);

        } catch (RuntimeException e) {
            System.err.println("Error fatal al finalizar la venta: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Transacción",
                    "No se pudo finalizar la venta. La operación fue revertida (ROLLBACK).\n" +
                            "Detalle: " + e.getCause().getMessage());
        }
    }

    // --- MÉTODO NUEVO PARA GENERAR EL PDF ---
    // 🚨 MODIFICADO: Ahora recibe el ID de la venta y el NIT/CF
    private void generarFacturaPDF(int idVenta, String nitCliente) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar factura de venta");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("Factura_Venta_" + idVenta + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        Stage stage = (Stage) btnGenerarFactura.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.setMargins(50, 50, 50, 50);

                // --- CABECERA ---
                document.add(new Paragraph("FACTURA DE VENTA").setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("AutomaticTienda S.A.").setFontSize(12).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("NIT 9390-24-26361").setFontSize(12).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("No. Factura: " + idVenta).setBold());

                // 🚨 NUEVO: INFORMACIÓN NIT/CF
                document.add(new Paragraph("NIT/CF: " + nitCliente).setBold());

                document.add(new Paragraph("venta: " + trabajadorLogueado.getUsuario() + " (ID: " + trabajadorLogueado.getId() + ")"));
                document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
                document.add(new Paragraph("--------------------------------------------------"));

                // --- TABLA DE ITEMS ---
                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2})).useAllAvailableWidth();
                table.addHeaderCell(new Cell().add(new Paragraph("Producto")).setBold());
                table.addHeaderCell(new Cell().add(new Paragraph("Cant.")).setBold());
                table.addHeaderCell(new Cell().add(new Paragraph("Subtotal")).setBold().setTextAlignment(TextAlignment.RIGHT));

                for (ItemCarrito item : itemsCarrito) {
                    table.addCell(new Cell().add(new Paragraph(item.getNombre())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getCantidad()))));
                    table.addCell(new Cell().add(new Paragraph(String.format("Q %.2f", item.getSubtotal()))).setTextAlignment(TextAlignment.RIGHT));
                }
                document.add(table);
                document.add(new Paragraph("\n"));

                // --- RESUMEN DE PAGO ---
                document.add(new Paragraph("RESUMEN DE PAGO").setBold().setTextAlignment(TextAlignment.RIGHT));
                document.add(new Paragraph("TOTAL: " + lblTotal.getText()).setFontSize(14).setBold().setTextAlignment(TextAlignment.RIGHT));
                document.add(new Paragraph("Método de Pago: " + cmbMetodoPago.getValue()).setTextAlignment(TextAlignment.RIGHT));

                if ("Efectivo".equals(cmbMetodoPago.getValue())) {
                    document.add(new Paragraph("Monto Recibido: Q" + txtMontoRecibido.getText()).setTextAlignment(TextAlignment.RIGHT));
                    document.add(new Paragraph("CAMBIO: " + lblCambio.getText()).setFontSize(14).setBold().setTextAlignment(TextAlignment.RIGHT));
                }

                document.add(new Paragraph("\nGracias por su compra.").setTextAlignment(TextAlignment.CENTER));

                document.close();

            } catch (FileNotFoundException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Archivo", "No se pudo crear el archivo PDF. Verifique si está abierto o si tiene permisos.");
                e.printStackTrace();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de PDF", "Ocurrió un error al generar el PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    // --- MÉTODOS AUXILIARES ---

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}