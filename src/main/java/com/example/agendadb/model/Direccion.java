package com.example.agendadb.model;

public class Direccion {
    private int id;
    private String direccionTexto;

    public Direccion() {}

    public Direccion(int id, String direccionTexto) {
        this.id = id;
        this.direccionTexto = direccionTexto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDireccionTexto() { return direccionTexto; }
    public void setDireccionTexto(String direccionTexto) { this.direccionTexto = direccionTexto; }

    @Override
    public String toString() {
        return direccionTexto;
    }
}