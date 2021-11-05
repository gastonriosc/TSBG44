package com.example.tsbhashtabledaapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class RegistrosControlador {

    private ArrayList deptos;
    private String []criterios;
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
    private TableView<?> tablaDatos;

    @FXML
    private Label txtnombreArchivo;

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
        String value = (String)cmbCriterio.getValue();
        if(value == "Por dosis"){
            cantidad_por_dosis();
        }

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

    public void cantidad_por_dosis()
    {
        int cont1 = 0 , cont2 = 0;
        if (chkTodos.isSelected()) {
            for (Registro r : tabla.values()) {
                if(r.getOrden_dosis() == "1")
                    cont1+= 1;
                else
                    cont2 +=1;
            }
        }
        else {
            for (Registro r : tabla.values()) {
                if (r.getJurisdiccion_aplicacion() == (String) cmbDepto.getValue()) {
                    if (r.getOrden_dosis() == "1")
                        cont1 += 1;
                    else
                        cont2 += 1;
                }
            }
        }

        tablaDatos.getColumns().clear();
        tablaDatos.getItems().clear();
        TableColumn NroDosis = new TableColumn("Nro dosis");
        TableColumn Cantidad = new TableColumn("Cantidad");
        tablaDatos.getColumns().addAll(NroDosis,Cantidad);
    }
}