package com.example.agendadb.dao;

import com.example.agendadb.model.Direccion;
import com.example.agendadb.model.Persona;
import com.example.agendadb.model.Telefono;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO implements IPersonaDAO {
    // ==========================================================
    // C - CREATE (Altas)
    // ==========================================================
    public boolean insertar(Persona persona) {
        String sqlPersona = "INSERT INTO Personas (nombre) VALUES (?)";
        String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
        String sqlPuente = "INSERT INTO Personas_Direcciones (persona_id, direccion_id) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Insertar la Persona (ya solo es el nombre)
            psmtPersona.setString(1, persona.getNombre());
            int filasAfectadas = psmtPersona.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = psmtPersona.getGeneratedKeys()) {
                    if (rs.next()) {
                        int personaIdGenerado = rs.getInt(1);
                        persona.setId(personaIdGenerado);

                        // 2. Insertar Teléfonos (Relación 1:N)
                        try (PreparedStatement psmtTelefono = conn.prepareStatement(sqlTelefono)) {
                            for (Telefono tel : persona.getTelefonos()) {
                                psmtTelefono.setInt(1, personaIdGenerado);
                                psmtTelefono.setString(2, tel.getTelefono());
                                psmtTelefono.executeUpdate();
                            }
                        }

                        // 3. Insertar Direcciones y crear el puente (Relación N:M)
                        try (PreparedStatement psmtPuente = conn.prepareStatement(sqlPuente)) {
                            for (Direccion dir : persona.getDirecciones()) {
                                int direccionId = buscarOCrearDireccion(conn, dir.getDireccionTexto());
                                psmtPuente.setInt(1, personaIdGenerado);
                                psmtPuente.setInt(2, direccionId);
                                psmtPuente.executeUpdate();
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
        // Consulta con JOIN para atravesar la tabla puente y traer las direcciones
        String sqlDirecciones = "SELECT d.id, d.direccion_texto FROM Direcciones d " +
                "INNER JOIN Personas_Direcciones pd ON d.id = pd.direccion_id " +
                "WHERE pd.persona_id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtPersonas = conn.prepareStatement(sqlPersonas);
             ResultSet rsPersonas = psmtPersonas.executeQuery()) {

            while (rsPersonas.next()) {
                Persona p = new Persona(rsPersonas.getInt("id"), rsPersonas.getString("nombre"));

                // Buscar Teléfonos
                try (PreparedStatement psmtTelefonos = conn.prepareStatement(sqlTelefonos)) {
                    psmtTelefonos.setInt(1, p.getId());
                    try (ResultSet rsTelefonos = psmtTelefonos.executeQuery()) {
                        while (rsTelefonos.next()) {
                            p.getTelefonos().add(new Telefono(
                                    rsTelefonos.getInt("id"),
                                    rsTelefonos.getInt("personaId"),
                                    rsTelefonos.getString("telefono")
                            ));
                        }
                    }
                }

                // Buscar Direcciones a través del JOIN
                try (PreparedStatement psmtDirecciones = conn.prepareStatement(sqlDirecciones)) {
                    psmtDirecciones.setInt(1, p.getId());
                    try (ResultSet rsDirecciones = psmtDirecciones.executeQuery()) {
                        while (rsDirecciones.next()) {
                            p.getDirecciones().add(new Direccion(
                                    rsDirecciones.getInt("id"),
                                    rsDirecciones.getString("direccion_texto")
                            ));
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
        String sqlActualizarPersona = "UPDATE Personas SET nombre = ? WHERE id = ?";
        String sqlEliminarTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertarTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        // Para N:M, borramos los puentes viejos y creamos nuevos
        String sqlEliminarPuentes = "DELETE FROM Personas_Direcciones WHERE persona_id = ?";
        String sqlInsertarPuente = "INSERT INTO Personas_Direcciones (persona_id, direccion_id) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement psmtActualizar = conn.prepareStatement(sqlActualizarPersona)) {

            // 1. Actualizar nombre
            psmtActualizar.setString(1, persona.getNombre());
            psmtActualizar.setInt(2, persona.getId());
            psmtActualizar.executeUpdate();

            // 2. Reemplazar Teléfonos
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

            // 3. Reemplazar Direcciones
            try (PreparedStatement psmtEliminarPuentes = conn.prepareStatement(sqlEliminarPuentes)) {
                psmtEliminarPuentes.setInt(1, persona.getId());
                psmtEliminarPuentes.executeUpdate();
            }
            try (PreparedStatement psmtInsertarPuente = conn.prepareStatement(sqlInsertarPuente)) {
                for (Direccion dir : persona.getDirecciones()) {
                    int direccionId = buscarOCrearDireccion(conn, dir.getDireccionTexto());
                    psmtInsertarPuente.setInt(1, persona.getId());
                    psmtInsertarPuente.setInt(2, direccionId);
                    psmtInsertarPuente.executeUpdate();
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
            // ON DELETE CASCADE en MariaDB se encarga de limpiar Telefonos y Personas_Direcciones
            return psmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar persona: " + e.getMessage());
        }
        return false;
    }

    // ==========================================================
    // MÉTODO AUXILIAR: El secreto de la relación Muchos a Muchos
    // ==========================================================
    private int buscarOCrearDireccion(Connection conn, String textoDireccion) throws SQLException {
        String sqlBuscar = "SELECT id FROM Direcciones WHERE direccion_texto = ?";
        try (PreparedStatement psmtBuscar = conn.prepareStatement(sqlBuscar)) {
            psmtBuscar.setString(1, textoDireccion);
            try (ResultSet rs = psmtBuscar.executeQuery()) {
                if (rs.next()) {
                    // La dirección ya existe, reutilizamos su ID
                    return rs.getInt("id");
                }
            }
        }

        // Si no existe, la insertamos como nueva
        String sqlInsertar = "INSERT INTO Direcciones (direccion_texto) VALUES (?)";
        try (PreparedStatement psmtInsertar = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            psmtInsertar.setString(1, textoDireccion);
            psmtInsertar.executeUpdate();
            try (ResultSet rs = psmtInsertar.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID de la dirección.");
    }
}