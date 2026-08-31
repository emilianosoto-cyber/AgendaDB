package com.example.agendadb;

import com.example.agendadb.dao.PersonaDAO;
import com.example.agendadb.model.Persona;
import com.example.agendadb.service.AgendaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class HelloController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefonos;
    @FXML private TableView<Persona> tablaPersonas;
    @FXML private TableColumn<Persona, Integer> colId;
    @FXML private TableColumn<Persona, String> colNombre;
    @FXML private TableColumn<Persona, String> colDireccion;
    @FXML private TableColumn<Persona, String> colTelefonos;

    // Alta cohesión: El controlador delega la lógica al servicio
    private final AgendaService agendaService;
    private ObservableList<Persona> listaPersonas;

    public HelloController() {
        // Inyectamos la dependencia concreta aquí
        this.agendaService = new AgendaService(new PersonaDAO());
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direcciones"));
        colTelefonos.setCellValueFactory(new PropertyValueFactory<>("telefonos"));

        cargarDatos();

        tablaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) llenarFormulario(newSel);
        });
    }

    private void cargarDatos() {
        listaPersonas = FXCollections.observableArrayList(agendaService.obtenerPersonas());
        tablaPersonas.setItems(listaPersonas);
    }

    private void llenarFormulario(Persona p) {
        txtNombre.setText(p.getNombre());
        txtDireccion.setText(p.getDirecciones().toString().replace("[", "").replace("]", ""));
        txtTelefonos.setText(p.getTelefonos().toString().replace("[", "").replace("]", ""));
    }

    @FXML
    protected void onGuardarClick() {
        boolean exito = agendaService.guardarPersona(0, txtNombre.getText(), txtDireccion.getText(), txtTelefonos.getText());
        if (exito) { cargarDatos(); onLimpiarClick(); }
        else { mostrarAlerta("Error", "No se pudo guardar."); }
    }

    @FXML
    protected void onActualizarClick() {
        Persona p = tablaPersonas.getSelectionModel().getSelectedItem();
        if (p == null) return;
        boolean exito = agendaService.guardarPersona(p.getId(), txtNombre.getText(), txtDireccion.getText(), txtTelefonos.getText());
        if (exito) { cargarDatos(); onLimpiarClick(); }
    }

    @FXML
    protected void onEliminarClick() {
        Persona p = tablaPersonas.getSelectionModel().getSelectedItem();
        if (p != null && agendaService.eliminarPersona(p.getId())) {
            cargarDatos(); onLimpiarClick();
        }
    }

    @FXML
    protected void onLimpiarClick() {
        txtNombre.clear(); txtDireccion.clear(); txtTelefonos.clear();
        tablaPersonas.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setContentText(mensaje); a.showAndWait();
    }
}