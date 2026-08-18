package com.example.agendadb.dao;

import com.example.agendadb.model.Persona;
import com.example.agendadb.model.Telefono;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    // ==========================================================
    // C - CREATE (Altas)
    // ==========================================================
    public boolean insertar(Persona persona) {
        String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
        String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        // PreparedStatement previene inyecciones SQL
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Insertar la Persona
            psmtPersona.setString(1, persona.getNombre());
            psmtPersona.setString(2, persona.getDireccion());
            int filasAfectadas = psmtPersona.executeUpdate();

            if (filasAfectadas > 0) {
                // 2. Obtener el ID autoincrementable que MariaDB le asignó a esta persona
                try (ResultSet rs = psmtPersona.getGeneratedKeys()) {
                    if (rs.next()) {
                        int personaIdGenerado = rs.getInt(1);
                        persona.setId(personaIdGenerado);

                        // 3. Insertar todos los teléfonos asociados a este nuevo ID
                        try (PreparedStatement psmtTelefono = conn.prepareStatement(sqlTelefono)) {
                            for (Telefono tel : persona.getTelefonos()) {
                                psmtTelefono.setInt(1, personaIdGenerado);
                                psmtTelefono.setString(2, tel.getTelefono());
                                psmtTelefono.executeUpdate();
                            }
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar persona: " + e.getMessage());
        }
        return false;
    }

    // ==========================================================
    // R - READ (Consultas)
    // ==========================================================
    public List<Persona> obtenerTodas() {
        List<Persona> listaPersonas = new ArrayList<>();
        String sqlPersonas = "SELECT * FROM Personas";
        String sqlTelefonos = "SELECT * FROM Telefonos WHERE personaId = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtPersonas = conn.prepareStatement(sqlPersonas);
             ResultSet rsPersonas = psmtPersonas.executeQuery()) {

            // Recorrer todas las personas
            while (rsPersonas.next()) {
                Persona p = new Persona(
                        rsPersonas.getInt("id"),
                        rsPersonas.getString("nombre"),
                        rsPersonas.getString("direccion")
                );

                // Por cada persona, buscar sus teléfonos
                try (PreparedStatement psmtTelefonos = conn.prepareStatement(sqlTelefonos)) {
                    psmtTelefonos.setInt(1, p.getId());
                    try (ResultSet rsTelefonos = psmtTelefonos.executeQuery()) {
                        while (rsTelefonos.next()) {
                            Telefono t = new Telefono(
                                    rsTelefonos.getInt("id"),
                                    rsTelefonos.getInt("personaId"),
                                    rsTelefonos.getString("telefono")
                            );
                            p.getTelefonos().add(t);
                        }
                    }
                }
                listaPersonas.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener personas: " + e.getMessage());
        }
        return listaPersonas;
    }

    // ==========================================================
    // U - UPDATE (Cambios/Modificaciones)
    // ==========================================================
    public boolean actualizar(Persona persona) {
        String sqlActualizarPersona = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
        String sqlEliminarTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertarTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtActualizar = conn.prepareStatement(sqlActualizarPersona)) {

            // 1. Actualizar datos de la persona
            psmtActualizar.setString(1, persona.getNombre());
            psmtActualizar.setString(2, persona.getDireccion());
            psmtActualizar.setInt(3, persona.getId());
            psmtActualizar.executeUpdate();

            // 2. Para los teléfonos, la estrategia más limpia es borrar los viejos y registrar los nuevos
            try (PreparedStatement psmtEliminarTel = conn.prepareStatement(sqlEliminarTelefonos)) {
                psmtEliminarTel.setInt(1, persona.getId());
                psmtEliminarTel.executeUpdate();
            }

            try (PreparedStatement psmtInsertarTel = conn.prepareStatement(sqlInsertarTelefono)) {
                for (Telefono tel : persona.getTelefonos()) {
                    psmtInsertarTel.setInt(1, persona.getId());
                    psmtInsertarTel.setString(2, tel.getTelefono());
                    psmtInsertarTel.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar persona: " + e.getMessage());
        }
        return false;
    }

    // ==========================================================
    // D - DELETE (Bajas)
    // ==========================================================
    public boolean eliminar(int idPersona) {
        String sql = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {

            psmt.setInt(1, idPersona);
            int filasAfectadas = psmt.executeUpdate();

            // Nota: No necesitamos hacer un DELETE explícito a la tabla Telefonos
            // porque al crear la base de datos le pusimos 'ON DELETE CASCADE'.
            // MariaDB borrará los teléfonos de esta persona automáticamente.
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar persona: " + e.getMessage());
        }
        return false;
    }
}