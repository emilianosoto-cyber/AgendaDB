package com.example.agendadb.service;

import com.example.agendadb.dao.IPersonaDAO;
import com.example.agendadb.model.Direccion;
import com.example.agendadb.model.Persona;
import com.example.agendadb.model.Telefono;
import java.util.List;

public class AgendaService {
    // DIP: Dependemos de la interfaz, no de la implementación
    private final IPersonaDAO personaDAO;

    public AgendaService(IPersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    public List<Persona> obtenerPersonas() {
        return personaDAO.obtenerTodas();
    }

    public boolean guardarPersona(int id, String nombre, String direccionesStr, String telefonosStr) {
        if (nombre == null || nombre.trim().isEmpty()) return false;

        Persona p = new Persona(id, nombre);
        procesarDirecciones(p, direccionesStr);
        procesarTelefonos(p, telefonosStr);

        if (id == 0) {
            return personaDAO.insertar(p);
        } else {
            return personaDAO.actualizar(p);
        }
    }

    public boolean eliminarPersona(int id) {
        return personaDAO.eliminar(id);
    }

    private void procesarDirecciones(Persona p, String direccionesStr) {
        p.getDirecciones().clear();
        String[] dirs = direccionesStr.split(",");
        for (String d : dirs) {
            if (!d.trim().isEmpty()) p.getDirecciones().add(new Direccion(0, d.trim()));
        }
    }

    private void procesarTelefonos(Persona p, String telefonosStr) {
        p.getTelefonos().clear();
        String[] tels = telefonosStr.split(",");
        for (String t : tels) {
            if (!t.trim().isEmpty()) p.getTelefonos().add(new Telefono(0, p.getId(), t.trim()));
        }
    }
}