package com.example.agendadb.dao;

import com.example.agendadb.model.Direccion;
import com.example.agendadb.model.Persona;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDAOTest {

    @Test
    void probarCicloDeVidaCRUD() {
        PersonaDAO dao = new PersonaDAO();

        // 1. CREATE: Probar Inserción
        Persona personaPrueba = new Persona(0, "Usuario Prueba");
        // Agregamos múltiples direcciones para probar la relación N:M
        personaPrueba.getDirecciones().add(new Direccion(0, "Calle Test 404"));
        personaPrueba.getDirecciones().add(new Direccion(0, "Avenida Prueba 505"));

        boolean insertado = dao.insertar(personaPrueba);

        assertTrue(insertado, "La inserción debe devolver true.");
        assertNotEquals(0, personaPrueba.getId(), "El ID autoincrementable debió actualizarse y no ser 0.");

        // 2. READ: Probar Consulta
        List<Persona> listaPersonas = dao.obtenerTodas();
        assertFalse(listaPersonas.isEmpty(), "La base de datos debe devolver al menos un registro.");

        // 3. UPDATE: Probar Actualización
        personaPrueba.setNombre("Usuario Modificado");
        personaPrueba.getDirecciones().clear(); // Borramos las anteriores
        personaPrueba.getDirecciones().add(new Direccion(0, "Calle Nueva 999")); // Ponemos una nueva

        boolean actualizado = dao.actualizar(personaPrueba);
        assertTrue(actualizado, "La actualización debe devolver true.");

        // 4. DELETE: Probar Eliminación
        boolean eliminado = dao.eliminar(personaPrueba.getId());
        assertTrue(eliminado, "La eliminación debe devolver true.");
    }
}