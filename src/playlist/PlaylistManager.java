/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import javafx.scene.control.TableView;
import javafx.scene.media.Media;
import util.ObservableObject;
import util.Observer;

/**
 *
 * @author sharv
 */
public class PlaylistManager implements Observer {

    private File playlistFile;
    private ObservableList<Song> songlist;
    private TableView<Song> tableView;
    
    private int currIndex;
    private Random r;

    public PlaylistManager(File newFile, TableView<Song> newTableView) {
        playlistFile = newFile;
        tableView = newTableView;
        
        if (playlistFile != null) {
            // Opened an existing playlist file
            // Load it
        }

        songlist = FXCollections.observableArrayList();
        r = new Random();

        currIndex = -1;
    }

    private boolean checkSong(Song newSong) {
        for (Song s : songlist) {
            if (s.getSongFile().getName().equals(newSong.getSongFile().getName())) {
                return true;
            }
        }
        return false;
    }

    public Song getNextSong() {
        if (songlist.isEmpty())
            return null;

        currIndex = (currIndex + 1) % songlist.size();
        return songlist.get(currIndex);
    }
    
    public Song getPreviousSong() {
        
        if (songlist.isEmpty())
            return null;
        
        currIndex--;
        
        if (currIndex < 0)
            currIndex = songlist.size() - 1;
        
        return songlist.get(currIndex);
    }

    public Song getCurrentSong() {
        if (songlist.isEmpty() || currIndex == -1)
            return null;
        
        return songlist.get(currIndex);
    }
    
    public void setCurrentSong(Song s) {
        currIndex = songlist.indexOf(s);
        updateTable();
    }
    
    public void deleteSong(Song s, boolean deletedCurrentSong) {
        songlist.remove(s);
        
        if (deletedCurrentSong)
            currIndex = -1;
        
        updateTable();
    }
    
    public void addSong(Song newSong) {

        if (checkSong(newSong)) {
            System.out.println("Song already in playlist");
            return;
        }
        
        songlist.add(newSong);

        newSong.addObserver(this);
        newSong.processMetadata();
        
        if (currIndex == -1) {
            currIndex = 0;
        }
    }
    
    public void addSongs(List<File> newSongs) {

        for (File f : newSongs) {
            
            Song s = new Song(f);
            s.setSongMedia(new Media(f.toURI().toString()));
            
            addSong(s);
        }
        
    }

    public void createShuffledQueue(Song currentSong) {
        songlist.remove(currentSong);
        Collections.shuffle(songlist);
        songlist.add(0, currentSong);
        currIndex = 0;
    }
     
    public void createSortedQueue(Song currentSong) {
        songlist.sort(new Comparator<Song>() {
            @Override
            public int compare(Song s1, Song s2) {
                return s1.getSongFile().getName().compareTo(s2.getSongFile().getName());
            }
        });
        currIndex = songlist.indexOf(currentSong);
    }
     
    public void printAll() {
        for (Song s : songlist) {
            System.out.println(s.getSongFile().getName());
        }
    }
    
    
    private void updateTable() {
        tableView.getItems().clear();
        for (Song s : songlist) {
            tableView.getItems().add(s);
        }
        
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run()
//            {
//                for (int i = 0; i < tableView.getItems().size(); i++) {
//                    if (i == currIndex)
//                        tableView.g.setStyle("-fx-background-color: rgb(255, 80, 80)");
//                    else    
//                        row.setStyle("");
//                }
//            }
//        });
    }
    
    public void save(File saveFile) {
        System.out.println("Saving playlist to " + saveFile.getAbsolutePath());
    }

    public File getPlaylistFile() {
        return playlistFile;
    }

    @Override
    public void update(ObservableObject observable) {
        updateTable();
    }
}
