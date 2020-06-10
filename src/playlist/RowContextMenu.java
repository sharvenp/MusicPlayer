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
 
    public RowContextMenu(MusicPlayerFXMLController newController, Song newSelectedSong) {
        
        MusicPlayerFXMLController controller = newController;
        Song selectedSong = newSelectedSong;
        
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem queueItem = new MenuItem("Queue Next");
        MenuItem removeItem = new MenuItem("Remove from Queue");
        
        deleteItem.setOnAction(event -> {
            controller.deleteSong(selectedSong);
        });
        
        queueItem.setOnAction(event -> {
            controller.queueSong(selectedSong);
        });
        
        removeItem.setOnAction(event -> {
            controller.removeSongFromQueue(selectedSong);
        });
        
        getItems().addAll(queueItem, deleteItem, removeItem);
    }
}
