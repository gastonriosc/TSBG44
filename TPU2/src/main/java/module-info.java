module interfaz.tpu2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens interfaz to javafx.fxml;
    exports interfaz;
}