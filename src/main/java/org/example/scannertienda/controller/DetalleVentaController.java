package org.example.scannertienda.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.scannertienda.model.DetalleVenta;

import java.util.List;

public class DetalleVentaController {

    @FXML private TableView<DetalleVenta> tablaDetalle;
    @FXML private TableColumn<DetalleVenta, String> colNombreProducto;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;
    @FXML private Label lblTotal;
    @FXML private Label lblIdVenta;

    @FXML
    public void initialize() {
        // Mapeo directo a los getters de DetalleVenta
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        // 🚨 Mapeo del campo Precio Unitario
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    public void setData(List<DetalleVenta> detalles, double total, int idVenta) {
        tablaDetalle.setItems(FXCollections.observableArrayList(detalles));
        lblTotal.setText(String.format("Q%.2f", total));
        lblIdVenta.setText(String.valueOf(idVenta));
    }
}