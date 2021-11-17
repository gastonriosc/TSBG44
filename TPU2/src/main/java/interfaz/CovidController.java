package interfaz;

import business.Depto;
import business.TablaDeptos;

import business.Vacuna;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class CovidController
{
    @FXML
    public ComboBox cmbCriterios;
    @FXML
    public CheckBox chkTodos;
    @FXML
    private Button btnProcesar;
    @FXML
    private Button btnSalir;
    @FXML
    private Button btnSeleccionar;
    @FXML
    private Button btnConsultar;
    @FXML
    private ComboBox cmbDeptos;
    @FXML
    private Label lblNombreArchivo;
    @FXML
    private Label lblSituacion;
    @FXML
    private TableView<?> tableDatos;

    private File f;
    private TablaDeptos deptos;

    public void initialize()
    {
        String []nombresdeptos = new String[]{"Capital", "Río Cuarto", "Colón", "San Justo", "Punilla",
                "General San Martín", "Tercero Arriba", "Unión", "Río Segundo", "Santa María", "Marcos Juárez",
                "Juárez Celman", "Calamuchita", "San Javier", "Cruz del Eje", "Río Primero", "San Alberto",
                "Presidente Roque Sáenz Peña", "General Roca", "Ischilín", "Totoral", "Río Seco", "Tulumba",
                "Sobremonte", "Pocho", "Minas"};

        cargarCombo(cmbDeptos, nombresdeptos);

        String []nombresCriterios = new String[]{"Por dosis", "Por sexo", "Por vacuna"};

        cargarCombo(cmbCriterios, nombresCriterios);
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
    void SalirClick(ActionEvent event)
    {
        Stage stage = (Stage) btnSalir.getScene().getWindow();
        stage.close();
    }
    @FXML
    void SeleccionarClick(ActionEvent event)
    {
        FileChooser selectorArchivo = new FileChooser();
        FileChooser.ExtensionFilter filtro = new FileChooser.ExtensionFilter("Archivos CSV", "*.csv");
        selectorArchivo.getExtensionFilters().add(filtro);
        selectorArchivo.setTitle("Seleccione el archivo a procesar");
        f = selectorArchivo.showOpenDialog(null);
        if (f != null)
        {
            lblNombreArchivo.setText(f.getName());
        }
    }
    @FXML
    void ProcesarClick(ActionEvent event)
    {
        lblSituacion.setText("Procesando, espere por favor...");

        deptos = new TablaDeptos();
        deptos.cargar(f);

        lblSituacion.setText("Archivo cargado con exito.");

        System.out.println(deptos);
    }


    public void ConsultarClick(ActionEvent event)
    {
        ObservableList ol;

        // aca hay que ver como recuperar el depto seleccionado del combo

        String nombre_depto = (String) cmbDeptos.getValue();
        //Depto departamento = deptos.getDepto();




        String criterio = (String) cmbCriterios.getValue();

        if (!chkTodos.isSelected())
        {
            // los print son para probar

            if(Objects.equals(criterio, "Por dosis"))
            {
//                tableDatos.getColumns().clear();
//                tableDatos.getItems().clear();
//                TableColumn NroDosis = new TableColumn("Nro dosis");
//                TableColumn Cantidad = new TableColumn("Cantidad");
//                tableDatos.getColumns().addAll(NroDosis,Cantidad);
//                ol = FXCollections.observableArrayList(departamento.getCantPrimeraVac(),departamento.getCantSegundaVac());
//                tableDatos.setItems(ol);

                System.out.println("Primera Dosis: " + departamento.getCantPrimeraVac());
                System.out.println("Segunda Dosis: " + departamento.getCantSegundaVac());
            }
            else if(Objects.equals(criterio, "Por sexo"))
            {
                System.out.println("Hombres: " + departamento.getCantVacunasHombre());
                System.out.println("Mujeres: " + departamento.getCantVacunasMujer());
            }
            else
            {
                for(Vacuna vac : departamento.getVacunas())
                {
                    System.out.println(vac);
                }
            }
        }
        else
        {

        }

    }
}