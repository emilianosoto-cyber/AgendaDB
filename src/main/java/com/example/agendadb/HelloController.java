package com.example.agendadb;

import com.example.agendadb.dao.PersonaDAO;
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

    // Se ejecuta automáticamente al abrir la ventana
    @FXML
    public void initialize() {
        // 1. Vincular las columnas de la tabla con los atributos de la clase Persona
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefonos.setCellValueFactory(new PropertyValueFactory<>("telefonos"));

        // 2. Cargar los datos desde MariaDB
        cargarDatos();

        // 3. Escuchar clics en la tabla para llenar el formulario automáticamente
        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                llenarFormulario(newSelection);
            }
        });
    }

    private void cargarDatos() {
        List<Persona> personas = personaDAO.obtenerTodas();
        // Las ObservableList notifican a la tabla cuando hay cambios
        listaPersonas = FXCollections.observableArrayList(personas);
        tablaPersonas.setItems(listaPersonas);
    }

    private void llenarFormulario(Persona p) {
        txtNombre.setText(p.getNombre());
        txtDireccion.setText(p.getDireccion());

        // Unir los teléfonos con comas para mostrarlos en la caja de texto
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

        Persona p = new Persona(0, txtNombre.getText(), txtDireccion.getText());

        // Separar los teléfonos por comas y agregarlos a la lista
        String[] tels = txtTelefonos.getText().split(",");
        for (String t : tels) {
            if (!t.trim().isEmpty()) {
                p.getTelefonos().add(new Telefono(0, 0, t.trim()));
            }
        }

        if (personaDAO.insertar(p)) {
            cargarDatos(); // Refrescar la tabla
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
        seleccionada.setDireccion(txtDireccion.getText());
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