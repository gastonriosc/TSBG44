package interfaz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CovidApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CovidApplication.class.getResource("Covid-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 650);
        stage.setTitle("Reporte vacunas COVID-19");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}