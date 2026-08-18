package com.example.agendadb;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {

        // 1. Prueba de conexión a MariaDB
        try (java.sql.Connection conn = com.example.agendadb.dao.ConexionDB.getConnection()) {
            if (conn != null) {
                System.out.println("¡Conexión exitosa a MariaDB!");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Fallo en la conexión: " + e.getMessage());
        }

        // 2. Lanza la interfaz gráfica de JavaFX
        Application.launch(HelloApplication.class, args);
    }
}