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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        controller.setScene(scene);
        controller.updateTheme();
        
        stage.setTitle("Tunez");
        stage.setScene(scene);
        stage.setResizable(false);
        
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("defaultArt.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
