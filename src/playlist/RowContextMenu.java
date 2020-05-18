/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import application.MusicPlayerFXMLController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 *
 * @author sharv
 */
public class RowContextMenu extends ContextMenu {
 
    private MenuItem deleteItem;
    private MenuItem queueItem;
    
    private MusicPlayerFXMLController controller;
    private Song selectedSong;
    
    public RowContextMenu(MusicPlayerFXMLController newController, Song newSelectedSong) {
        
        controller = newController;
        selectedSong = newSelectedSong;
        
        deleteItem = new MenuItem("Delete");
        queueItem = new MenuItem("Queue Next");
    
        
        deleteItem.setOnAction(event -> {
            controller.deleteSong(selectedSong);
        });
        
        queueItem.setOnAction(event -> {
            controller.queueSong(selectedSong);
        });
        
        getItems().addAll(queueItem, deleteItem);
    }
}
