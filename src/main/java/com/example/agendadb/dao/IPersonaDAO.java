package com.example.agendadb.dao;
import com.example.agendadb.model.Persona;
import java.util.List;

public interface IPersonaDAO {
    boolean insertar(Persona persona);
    List<Persona> obtenerTodas();
    boolean actualizar(Persona persona);
    boolean eliminar(int idPersona);
}