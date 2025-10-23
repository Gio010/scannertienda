package org.example.scannertienda.model;

public class Usuario {
    private int id;
    private String usuario;
    private String passwordSimple;
    private String rol;

    // Constructor completo
    public Usuario(int id, String usuario, String passwordSimple, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.passwordSimple = passwordSimple;
        this.rol = rol;
    }

    // Constructor sin ID (para crear nuevo usuario)
    public Usuario(String usuario, String passwordSimple, String rol) {
        this.usuario = usuario;
        this.passwordSimple = passwordSimple;
        this.rol = rol;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPasswordSimple() { return passwordSimple; }
    public void setPasswordSimple(String passwordSimple) { this.passwordSimple = passwordSimple; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}