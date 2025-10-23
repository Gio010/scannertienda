package org.example.scannertienda.controller;

// JavaFX Imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.itextpdf.layout.properties.UnitValue;

// Java/Util Imports
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

// iText 7 Imports
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import org.example.scannertienda.dao.VentaDAO;
import org.example.scannertienda.model.Venta;
import org.example.scannertienda.dao.DetalleVentaDAO;
import org.example.scannertienda.model.DetalleVenta;

public class ReporteController implements Initializable {

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, Integer> colID;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, LocalDateTime> colFecha;
    @FXML private TableColumn<Venta, Integer> colTrabajador;

    private VentaDAO ventaDAO = new VentaDAO();
    private DetalleVentaDAO detalleDAO = new DetalleVentaDAO();
    private ObservableList<Venta> listaVentas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar las columnas
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTrabajador.setCellValueFactory(new PropertyValueFactory<>("idTrabajador"));

        // 2. Asignar la lista a la tabla
        tablaVentas.setItems(listaVentas);

        // 3. Cargar datos al iniciar
        cargarVentas();

        // 4. DAR FORMATO A LA COLUMNA DE FECHA
        colFecha.setCellFactory(column -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss");

            return new TableCell<Venta, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
    }

    public void cargarVentas() {
        try {
            listaVentas.clear();
            List<Venta> ventasDB = ventaDAO.obtenerTodasLasVentas();
            listaVentas.addAll(ventasDB);
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de BD", "No se pudieron cargar las ventas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void generarReportePDF(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Ventas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("Reporte_Ventas_" + java.time.LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Inicializar PDF
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                // 1. TÍTULO Y ENCABEZADO
                document.add(new Paragraph("Reporte historico de ventas")
                        .setFontSize(20)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Generado el: " + java.time.LocalDateTime.now().format(formatter)));
                document.add(new Paragraph(""));

                // 2. TABLA DE RESUMEN DE VENTAS (Tu código original)
                document.add(new Paragraph("Resumen General de Ventas").setFontSize(14).setBold());
                document.add(new Paragraph(""));

                float[] columnWidths = {1, 2, 4, 1.5f};
                Table summaryTable = new Table(columnWidths);
                summaryTable.setWidth(UnitValue.createPercentValue(100)); // Asegura que ocupe todo el ancho.

                // Añadir encabezados
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("ID Venta").setBold()));
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Total (Q)").setBold()));
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("Fecha y Hora").setBold()));
                summaryTable.addHeaderCell(new Cell().add(new Paragraph("ID Trabajador").setBold()));

                // Llenar la tabla del resumen
                for (Venta venta : listaVentas) {
                    summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getId()))));
                    summaryTable.addCell(new Cell().add(new Paragraph(String.format("Q %.2f", venta.getTotal()))));
                    String fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(formatter) : "N/A";
                    summaryTable.addCell(new Cell().add(new Paragraph(fechaFormateada)));
                    summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(venta.getIdTrabajador()))));
                }

                document.add(summaryTable);

                // 3. DETALLE DE PRODUCTOS POR VENTA (NUEVA SECCIÓN)

                document.add(new Paragraph("\nDetalle de Productos Vendidos").setFontSize(14).setBold());

                for (Venta venta : listaVentas) {
                    // Título de la venta detallada
                    String fechaVentaStr = venta.getFecha() != null ? venta.getFecha().format(formatter) : "N/A";
                    document.add(new Paragraph("\n--- VENTA #" + venta.getId() + " (Total: Q" + String.format("%.2f", venta.getTotal()) + ") ---")
                            .setFontSize(10).setBold());

                    // Obtener los detalles de la venta desde la BD
                    List<DetalleVenta> detalles = detalleDAO.obtenerDetallesPorVenta(venta.getId());

                    if (!detalles.isEmpty()) {
                        // Crear la tabla de detalle (anidada)
                        float[] detailColumnWidths = {4, 1, 1.5f, 1.5f}; // Nombre, Cantidad, P. Unitario, Subtotal
                        Table detailTable = new Table(detailColumnWidths);
                        detailTable.setWidth(UnitValue.createPercentValue(90));
                        detailTable.setMarginLeft(30);

                        // Encabezados del detalle
                        detailTable.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold().setFontSize(9)));
                        detailTable.addHeaderCell(new Cell().add(new Paragraph("Cant.").setBold().setFontSize(9)));
                        detailTable.addHeaderCell(new Cell().add(new Paragraph("P. Unitario").setBold().setFontSize(9)));
                        detailTable.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold().setFontSize(9)));

                        // Llenar filas del detalle
                        for (DetalleVenta detalle : detalles) {
                            detailTable.addCell(new Cell().add(new Paragraph(detalle.getNombreProducto()).setFontSize(8)));
                            detailTable.addCell(new Cell().add(new Paragraph(String.valueOf(detalle.getCantidad())).setFontSize(8)));
                            detailTable.addCell(new Cell().add(new Paragraph(String.format("Q %.2f", detalle.getPrecioUnitario())).setFontSize(8)));
                            // El modelo DetalleVenta tiene el getter getSubtotalItem() que calcula (precio * cantidad)
                            detailTable.addCell(new Cell().add(new Paragraph(String.format("Q %.2f", detalle.getSubtotalItem())).setFontSize(8)));
                        }

                        document.add(detailTable);
                    } else {
                        document.add(new Paragraph("    (Sin detalles de productos registrados para esta venta)").setFontSize(8).setItalic());
                    }
                }

                // Cerrar el documento
                document.close();

                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Reporte PDF generado correctamente en:\n" + file.getAbsolutePath());

            } catch (FileNotFoundException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Archivo", "No se pudo escribir el archivo. Verifique los permisos.");
                e.printStackTrace();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de PDF", "Ocurrió un error al generar el PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}