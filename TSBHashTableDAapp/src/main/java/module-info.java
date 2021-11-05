module com.example.tsbhashtabledaapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tsbhashtabledaapp to javafx.fxml;
    exports com.example.tsbhashtabledaapp;
}