package com.example.agendadb.model;

import java.util.ArrayList;
import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private List<Telefono> telefonos;
    // NUEVO: Ahora soporta múltiples direcciones
    private List<Direccion> direcciones;

    public Persona() {
        this.telefonos = new ArrayList<>();
        this.direcciones = new ArrayList<>();
    }

    public Persona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.telefonos = new ArrayList<>();
        this.direcciones = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Telefono> getTelefonos() { return telefonos; }
    public void setTelefonos(List<Telefono> telefonos) { this.telefonos = telefonos; }

    public List<Direccion> getDirecciones() { return direcciones; }
    public void setDirecciones(List<Direccion> direcciones) { this.direcciones = direcciones; }
}