module scannertienda {
    requires java.sql; //se agrego modulo de sql para la conexion
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens org.example.scannertienda to javafx.fxml, javafx.graphics;
    exports org.example.scannertienda;

}