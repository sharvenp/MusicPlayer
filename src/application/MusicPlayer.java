/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author sharv
 */
public class MusicPlayer extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MusicPlayerFXML.fxml"));
        Parent root = (Parent) loader.load();

        MusicPlayerFXMLController controller = (MusicPlayerFXMLController) loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root);
        stage.setTitle("MusicPlayer");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
