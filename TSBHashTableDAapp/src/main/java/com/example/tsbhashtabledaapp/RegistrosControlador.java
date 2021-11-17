package com.example.tsbhashtabledaapp;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import soporte.CantidadPorDosis;
import soporte.Registro;
import soporte.TSBHashTableDA;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Controlador de la Escena Registros.
 * @author Gaston Ríos Cardona, Pilar Ávila, Juan Cruz Targón, Micaela Ardiles.
 * @version Noviembre de 2021.
 */
public class RegistrosControlador {

    /**
     * ArrayList de departamentos para cargar en el combo.
     */
    private ArrayList deptos;
    /**
     * ArrayList de criterios para cargar en el combo.
     */
    private String []criterios;
    /**
     * Tabla donde se guardan los registros.
     */
    private TSBHashTableDA<Integer, Registro> tabla;

    @FXML
    private Button btnAbrir;

    @FXML
    private CheckBox chkTodos;

    @FXML
    private ComboBox<?> cmbCriterio;

    @FXML
    private ComboBox<?> cmbDepto;

    @FXML
    private TableView<CantidadPorDosis> tablaDatos;

    @FXML
    private Label txtnombreArchivo;

    @FXML
    private Button btnConsultar;

    @FXML

    void AbrirClick(ActionEvent event) {
        FileChooser selectorArchivo = new FileChooser();
        FileChooser.ExtensionFilter filtro = new FileChooser.ExtensionFilter("Archivos CSV", "*.csv");
        //
        selectorArchivo.getExtensionFilters().add(filtro);

        selectorArchivo.setTitle("Seleccione el archivo a procesar");
        File f =selectorArchivo.showOpenDialog(null);

        txtnombreArchivo.setText(f.getName());

        tabla = new TSBHashTableDA<>();
        int cont = 0;

        try
        {
            Scanner miEscaner = new Scanner(f);
            while (miEscaner.hasNextLine())
            {
                String linea = miEscaner.nextLine();
                String []line = linea.split(",");
                if (Objects.equals(line[6], "\"Córdoba\"")) {
                    Registro reg = new Registro(line[0], line[1], line[2], line[3],
                            line[4], line[5], line[6], line[7],
                            line[8], line[9], line[10], line[11],
                            line[12], line[13], line[14]);
                    tabla.put(cont, reg);

                    cont += 1;
                }
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("El arch no existe");
        }

        cmbDepto.setDisable(false);
        chkTodos.setDisable(false);
    }

    @FXML
    void TodosChecked(ActionEvent event) {
        if(chkTodos.isSelected()) {
            cmbDepto.setDisable(true);
            cmbCriterio.setDisable(false);
        }
        else {
            cmbDepto.setDisable(false);
            cmbCriterio.setDisable(true);
        }
    }

    @FXML
    void criterioSelected(ActionEvent event) {
        btnConsultar.setDisable(false);
    }

    @FXML
    void deptoSelected(ActionEvent event) {
        String value = (String)cmbDepto.getValue();
        if(!Objects.equals(value, "")) {
            cmbCriterio.setDisable(false);
        }
    }

    public void initialize()
    {
        cmbDepto.setDisable(true);
        cmbCriterio.setDisable(true);
        chkTodos.setDisable(true);
        btnConsultar.setDisable(true);

        String []nombresdeptos = new String[]{"Capital", "Río Cuarto", "Colón", "San Justo", "Punilla",
                "General San Martín", "Tercero Arriba", "Unión", "Río Segundo", "Santa María", "Marcos Juárez",
                "Juárez Celman", "Calamuchita", "San Javier", "Cruz del Eje", "Río Primero", "San Alberto",
                "Presidente Roque Sáenz Peña", "General Roca", "Ischilín", "Totoral", "Río Seco", "Tulumba",
                "Sobremonte", "Pocho", "Minas"};

       cargarCombo(cmbDepto, nombresdeptos);

       String []nombresCriterios = new String[]{"Por dosis", "Por sexo", "Por vacuna"};

       cargarCombo(cmbCriterio, nombresCriterios);
    }

    private void cargarCombo(ComboBox combo, String []lista)
    {
        ArrayList list = new ArrayList();
        for (int i = 0; i< lista.length; i++)
        {
            list.add(lista[i]);
        }

        combo.getItems().addAll(list);
    }

    @FXML
    void ConsultarClick(ActionEvent event) {
        String criterio = (String) cmbCriterio.getValue();
        if(Objects.equals(criterio, "Por dosis"))
        {
            cantidad_por_dosis();
        }
        else if(Objects.equals(criterio, "Por sexo"))
        {
            cantidad_por_sexo();
        }
        else if(Objects.equals(criterio, "Por vacuna"))
        {
            cantidad_por_vacuna();
        }
    }

    public void cantidad_por_dosis()
    {
        int cont1 = 0 , cont2 = 0;
        if (chkTodos.isSelected()) {
            for(int i = 0; i< tabla.size(); i++)
            {
                if(Objects.equals(tabla.get(i).getOrden_dosis(), "1"))
                    cont1+= 1;
                else if (Objects.equals(tabla.get(i).getOrden_dosis(), "2"))
                    cont2 +=1;
            }
        }
        else {
            String value = (String) cmbDepto.getValue();
            for(int i = 0; i< tabla.size(); i++) {
                if (Objects.equals(tabla.get(i).getDepto_aplicacion(), "\"" + value + "\"")) {
                    if(Objects.equals(tabla.get(i).getOrden_dosis(), "1"))
                        cont1+= 1;
                    else if (Objects.equals(tabla.get(i).getOrden_dosis(), "2"))
                        cont2 +=1;
                }
            }
        }

        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();

        CantidadPorDosis cantX1Dosis = new CantidadPorDosis("Primera", cont1);
        CantidadPorDosis cantX2Dosis = new CantidadPorDosis("Segunda", cont2);
        ObservableList<CantidadPorDosis> dosis = FXCollections.observableArrayList();
        dosis.add(cantX1Dosis);
        dosis.add(cantX2Dosis);

        TableColumn<CantidadPorDosis, String> NroDosis = new TableColumn<> ("Nro dosis");
        TableColumn<CantidadPorDosis, Integer> Cantidad = new TableColumn<> ("Cantidad");
        NroDosis.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, String>("valor"));
        Cantidad.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, Integer>("cantidad"));
        NroDosis.setMinWidth(200);
        Cantidad.setMinWidth(270);
        tablaDatos.getColumns().addAll(NroDosis, Cantidad);
        tablaDatos.getItems().addAll(dosis);
    }

    public void cantidad_por_sexo()
    {
        int contM = 0 , contF = 0;
        if (chkTodos.isSelected()) {
            for(int i = 0; i< tabla.size(); i++)
            {
                if(Objects.equals(tabla.get(i).getSexo(), "\"M\""))
                    contM += 1;
                else if (Objects.equals(tabla.get(i).getSexo(), "\"F\""))
                    contF +=1;
            }
        }
        else {
            String value = (String) cmbDepto.getValue();
            for(int i = 0; i< tabla.size(); i++) {
                if (Objects.equals(tabla.get(i).getDepto_aplicacion(), "\"" + value + "\"")) {
                    if(Objects.equals(tabla.get(i).getSexo(), "\"M\""))
                        contM += 1;
                    else if (Objects.equals(tabla.get(i).getSexo(), "\"F\""))
                        contF +=1;
                }
            }
        }
        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();

        CantidadPorDosis cantXSexoM = new CantidadPorDosis("Masculino", contM);
        CantidadPorDosis cantXSexoF = new CantidadPorDosis("Femenino", contF);
        ObservableList<CantidadPorDosis> sexos = FXCollections.observableArrayList();
        sexos.add(cantXSexoM);
        sexos.add(cantXSexoF);

        TableColumn<CantidadPorDosis, String> genero = new TableColumn<> ("Género");
        TableColumn<CantidadPorDosis, Integer> cantidad = new TableColumn<> ("Cantidad");
        genero.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, String>("valor"));
        cantidad.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, Integer>("cantidad"));
        genero.setMinWidth(200);
        cantidad.setMinWidth(270);
        tablaDatos.getColumns().addAll(genero, cantidad);
        tablaDatos.getItems().addAll(sexos);
    }

    private void cantidad_por_vacuna()
    {
        int contSinopharm = 0 , contPfizer = 0, contModerna = 0 , contAstraZeneca = 0, contSputnik = 0;
        if (chkTodos.isSelected()) {
            for(int i = 0; i< tabla.size(); i++)
            {
                if(Objects.equals(tabla.get(i).getVacuna(), "\"Sinopharm\""))
                    contSinopharm += 1;
                else if (Objects.equals(tabla.get(i).getVacuna(), "\"Pfizer\""))
                    contPfizer +=1;
                else if (Objects.equals(tabla.get(i).getVacuna(), "\"Moderna\""))
                    contModerna +=1;
                else if (Objects.equals(tabla.get(i).getVacuna(), "\"AstraZeneca\""))
                    contAstraZeneca +=1;
                else if (Objects.equals(tabla.get(i).getVacuna(), "\"Sputnik\""))
                    contSputnik +=1;
            }
        }
        else {
            String value = (String) cmbDepto.getValue();
            for(int i = 0; i< tabla.size(); i++) {
                if (Objects.equals(tabla.get(i).getDepto_aplicacion(), "\"" + value + "\"")) {
                    if(Objects.equals(tabla.get(i).getVacuna(), "\"Sinopharm\""))
                        contSinopharm += 1;
                    else if (Objects.equals(tabla.get(i).getVacuna(), "\"Pfizer\""))
                        contPfizer +=1;
                    else if (Objects.equals(tabla.get(i).getVacuna(), "\"Moderna\""))
                        contModerna +=1;
                    else if (Objects.equals(tabla.get(i).getVacuna(), "\"AstraZeneca\""))
                        contAstraZeneca +=1;
                    else if (Objects.equals(tabla.get(i).getVacuna(), "\"Sputnik\""))
                        contSputnik +=1;
                }
            }
        }
        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();

        CantidadPorDosis cantXSinopharm = new CantidadPorDosis("Sinopharm", contSinopharm);
        CantidadPorDosis cantXPfizer = new CantidadPorDosis("Pfizer", contPfizer);
        CantidadPorDosis cantXModerna = new CantidadPorDosis("Moderna", contModerna);
        CantidadPorDosis cantXAstraZeneca = new CantidadPorDosis("AstraZeneca", contAstraZeneca);
        CantidadPorDosis cantXSputnik = new CantidadPorDosis("Sputnik", contSputnik);

        ObservableList<CantidadPorDosis> vacunas = FXCollections.observableArrayList();
        vacunas.add(cantXSinopharm);
        vacunas.add(cantXPfizer);
        vacunas.add(cantXModerna);
        vacunas.add(cantXAstraZeneca);
        vacunas.add(cantXSputnik);

        TableColumn<CantidadPorDosis, String> vacuna = new TableColumn<> ("Vacuna");
        TableColumn<CantidadPorDosis, Integer> cantidad = new TableColumn<> ("Cantidad");
        vacuna.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, String>("valor"));
        cantidad.setCellValueFactory(new PropertyValueFactory<CantidadPorDosis, Integer>("cantidad"));
        vacuna.setMinWidth(200);
        cantidad.setMinWidth(270);
        tablaDatos.getColumns().addAll(vacuna, cantidad);
        tablaDatos.getItems().addAll(vacunas);
    }
}