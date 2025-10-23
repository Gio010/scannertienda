module scannertienda {
    requires java.sql; //se agrego modulo de sql para la conexion
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    requires layout;
    requires kernel;
    requires io;
    requires itext.commons;

    opens org.example.scannertienda to javafx.fxml, javafx.graphics;
    exports org.example.scannertienda;
    exports org.example.scannertienda.model;
    opens org.example.scannertienda.model to javafx.fxml, javafx.graphics;
    exports org.example.scannertienda.dao;
    opens org.example.scannertienda.dao to javafx.fxml, javafx.graphics;
    exports org.example.scannertienda.util;
    opens org.example.scannertienda.util to javafx.fxml, javafx.graphics;
    exports org.example.scannertienda.controller;
    opens org.example.scannertienda.controller to javafx.fxml, javafx.graphics;

}