/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * FXML Controller class
 *
 * @author sharv
 */
public class MusicPlayerFXMLController implements Initializable {

    @FXML
    private Slider songSlder;
    @FXML
    private Label songNameLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void toggleSong(ActionEvent event) {
        
    }

    @FXML
    private void nextSong(ActionEvent event) {
    }

    @FXML
    private void previousSong(ActionEvent event) {
    }

    @FXML
    private void toggleLoop(ActionEvent event) {
    }

    @FXML
    private void toggleShuffle(ActionEvent event) {
    }
    
}
