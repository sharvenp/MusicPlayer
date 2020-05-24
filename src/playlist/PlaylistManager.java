/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    private TableView<Song> queueTable;
    private ArrayList<Song> songQueue;
    
    private int currIndex;
    private Random r;
    
    private boolean shuffle;

    public PlaylistManager(File newFile, TableView<Song> newTableView, TableView<Song> newQueueTable) {
        playlistFile = newFile;
        tableView = newTableView;
        queueTable = newQueueTable;

        songlist = FXCollections.observableArrayList();
        songQueue = new ArrayList<>();
        r = new Random();
 
        currIndex = -1;

        if (newFile != null) {
            load(newFile);
        }
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
        return songQueue.get(currIndex);
    }
    
    public Song getPreviousSong() {
        
        if (songlist.isEmpty())
            return null;
        
        currIndex--;
        if (currIndex < 0)
            currIndex = songlist.size() - 1;
        
        return songQueue.get(currIndex);
    }

    public Song getCurrentSong() {
        if (songQueue.isEmpty() || currIndex == -1)
            return null;
        
        return songQueue.get(currIndex);
    }
    
    public void setCurrentSong(Song s) {
        currIndex = songQueue.indexOf(s);
        
        if (shuffle)
            createShuffledQueue(s);
        else
            createSortedQueue(s);
        
        updateTable();
    }
    
    public void deleteSong(Song s, boolean deletedCurrentSong) {
        songlist.remove(s);
        songQueue.remove(s);
        
        if (deletedCurrentSong)
            currIndex = -1;
        
        updateTable();
    }
    
    public void queueSong(Song s) {
        songQueue.remove(s);
        if (currIndex < songQueue.size() - 1) {
            songQueue.add(currIndex + 1, s);
        } else {
            songQueue.add(s);
        }
        
        updateQueue();
    }
    
    public void createShuffledQueue(Song currentSong) {
        songQueue.clear();
        for (Song s : songlist) {
            if (s != currentSong)
                songQueue.add(s);
        }
        Collections.shuffle(songQueue);
        songQueue.add(0, currentSong);
        currIndex = 0;
        
        updateQueue();
    }
     
    public void createSortedQueue(Song currentSong) {
        
        songQueue.clear();
        for (Song s : songlist) {
            songQueue.add(s);
        }
        currIndex = songQueue.indexOf(currentSong);
    
        updateQueue();
    }
    
    public void addSong(Song newSong) {

        if (checkSong(newSong)) {
            System.out.println("Song already in playlist");
            return;
        }
        
        songlist.add(newSong);
        songQueue.add(newSong);

        if (currIndex == -1) {
            currIndex = 0;
        }
        
        newSong.addObserver(this);
        newSong.processMetadata();
    }
    
    public void addSongs(List<File> newSongs) {

        for (File f : newSongs) {
            
            Song s = new Song(f);
            s.setSongMedia(new Media(f.toURI().toString()));
            
            addSong(s);
        }
        
    }
     
    public void printAll() {
        System.out.println("Queue: ");
        for (Song s : songQueue) {
            System.out.println(s.getSongFile().getName());
        }
    }
    
    private void updateQueue() {
        queueTable.getItems().clear();
        for (int i = currIndex + 1; i < songQueue.size(); i++) {
            queueTable.getItems().add(songQueue.get(i));
        }
    }
    
    private void updateTable() {
        tableView.getItems().clear();
        for (Song s : songlist) {
            tableView.getItems().add(s);
        }

        updateQueue();
    }
    
    public void load(File loadFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loadFile));
            String line = reader.readLine();
            int i = 0;
            while (line != null) {
               
                File songFile = new File(line);
                Song s = new Song(songFile);
                Media songMedia = new Media(songFile.toURI().toString());
                s.setSongMedia(songMedia);
                
                addSong(s);
                
                i++;
                
                line = reader.readLine();
            }
            System.out.println("Read " + i + " songs.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save(File saveFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            for (Song s : songlist) {
                writer.write(s.getSongFile().getParentFile() + File.separator + s.getSongFile().getName());
                writer.newLine();
            }    
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getPlaylistFile() {
        return playlistFile;
    }
    
    public ObservableList<Song> getSongList() {
        return songlist;
    }

    public void setShuffle(boolean newVal) {
        shuffle = newVal;
    }
    
    @Override
    public void update(ObservableObject observable) {
        updateTable();
    }
}
