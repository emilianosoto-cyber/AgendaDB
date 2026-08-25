package com.example.agendadb;

import com.example.agendadb.dao.PersonaDAO;
import com.example.agendadb.model.Direccion;
import com.example.agendadb.model.Persona;
import com.example.agendadb.model.Telefono;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HelloController {

    // Componentes de la vista
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefonos;

    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, Integer> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colDireccion;
    @FXML private TableColumn<Persona, String> colTelefonos;

    private PersonaDAO personaDAO = new PersonaDAO();
    private ObservableList<Persona> listaPersonas;

    @FXML
    public void initialize() {
        // 1. Vincular las columnas (Nota que ahora busca 'direcciones' en plural)
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direcciones"));
        colTelefonos.setCellValueFactory(new PropertyValueFactory<>("telefonos"));

        cargarDatos();

        // 2. Escuchar clics en la tabla
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                llenarFormulario(newSelection);
            }
        });
    }

    private void cargarDatos() {
        List<Persona> personas = personaDAO.obtenerTodas();
        listaPersonas = FXCollections.observableArrayList(personas);
        tablaPersonas.setItems(listaPersonas);
    }

    private void llenarFormulario(Persona p) {
        txtNombre.setText(p.getNombre());

        // Unir las direcciones con comas para la caja de texto
        StringBuilder dirs = new StringBuilder();
        for (int i = 0; i < p.getDirecciones().size(); i++) {
            dirs.append(p.getDirecciones().get(i).getDireccionTexto());
            if (i < p.getDirecciones().size() - 1) {
                dirs.append(", ");
            }
        }
        txtDireccion.setText(dirs.toString());

        // Unir los teléfonos con comas para la caja de texto
        StringBuilder tels = new StringBuilder();
        for (int i = 0; i < p.getTelefonos().size(); i++) {
            tels.append(p.getTelefonos().get(i).getTelefono());
            if (i < p.getTelefonos().size() - 1) {
                tels.append(", ");
            }
        }
        txtTelefonos.setText(tels.toString());
    }

    @FXML
    protected void onGuardarClick() {
        if (txtNombre.getText().isEmpty()) {
            mostrarAlerta("Error", "El nombre es obligatorio.");
            return;
        }

        // El constructor ya no pide la dirección, solo el nombre
        Persona p = new Persona(0, txtNombre.getText());

        // Procesar las múltiples direcciones separadas por coma
        String[] dirs = txtDireccion.getText().split(",");
        for (String d : dirs) {
            if (!d.trim().isEmpty()) {
                p.getDirecciones().add(new Direccion(0, d.trim()));
            }
        }

        // Procesar los teléfonos
        String[] tels = txtTelefonos.getText().split(",");
        for (String t : tels) {
            if (!t.trim().isEmpty()) {
                p.getTelefonos().add(new Telefono(0, 0, t.trim()));
            }
        }

        if (personaDAO.insertar(p)) {
            cargarDatos();
            onLimpiarClick();
        } else {
            mostrarAlerta("Error", "No se pudo guardar la persona.");
        }
    }

    @FXML
    protected void onActualizarClick() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una persona de la tabla para actualizar.");
            return;
        }

        seleccionada.setNombre(txtNombre.getText());

        // Limpiar direcciones anteriores y agregar las nuevas
        seleccionada.getDirecciones().clear();
        String[] dirs = txtDireccion.getText().split(",");
        for (String d : dirs) {
            if (!d.trim().isEmpty()) {
                seleccionada.getDirecciones().add(new Direccion(0, d.trim()));
            }
        }

        // Limpiar teléfonos anteriores y agregar los nuevos
        seleccionada.getTelefonos().clear();
        String[] tels = txtTelefonos.getText().split(",");
        for (String t : tels) {
            if (!t.trim().isEmpty()) {
                seleccionada.getTelefonos().add(new Telefono(0, seleccionada.getId(), t.trim()));
            }
        }

        if (personaDAO.actualizar(seleccionada)) {
            cargarDatos();
            onLimpiarClick();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la persona.");
        }
    }

    @FXML
    protected void onEliminarClick() {
        Persona seleccionada = tablaPersonas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona una persona de la tabla para eliminar.");
            return;
        }

        if (personaDAO.eliminar(seleccionada.getId())) {
            cargarDatos();
            onLimpiarClick();
        } else {
            mostrarAlerta("Error", "No se pudo eliminar la persona.");
        }
    }

    @FXML
    protected void onLimpiarClick() {
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefonos.clear();
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}