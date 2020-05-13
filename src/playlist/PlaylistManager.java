/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javafx.scene.control.TableView;

/**
 *
 * @author sharv
 */
public class PlaylistManager {

    private File playlistFile;
    private LinkedList<File> songlist;
    private TableView<Object> tableView;
    
    private int currIndex;
    private Random r;

    public PlaylistManager(File newFile, TableView<Object> tableView) {
        playlistFile = newFile;
        this.tableView = tableView;
        
        if (playlistFile != null) {
            // Opened an existing playlist file
        }

        songlist = new LinkedList<>();

        r = new Random();

        currIndex = -1;
    }

    private boolean checkSong(File newSong) {
        for (File f : songlist) {
            if (f.getName().equals(newSong.getName())) {
                return true;
            }
        }
        return false;
    }

    public File getNextSong() {
        if (songlist.size() == 0)
            return null;

        currIndex = (currIndex + 1) % songlist.size();
        return songlist.get(currIndex);
    }
    
    public File getPreviousSong() {
        
        if (songlist.size() == 0)
            return null;
        
        
        currIndex--;
        
        if (currIndex < 0)
            currIndex = songlist.size() - 1;
        
        return songlist.get(currIndex);
    }

    public void addSong(File newSong) {

        if (checkSong(newSong)) {
            return;
        }

        songlist.add(newSong);

        if (currIndex == -1) {
            currIndex = 0;
        }
    }
    
    public void addSongs(List<File> newSongs) {

        for (File song : newSongs) {
            
            if (checkSong(song)) {
                return;
            }

            songlist.add(song);

        }

        if (currIndex == -1) {
            currIndex = 0;
        }
    }

    public void createShuffledQueue(File currentSong) {
        
        songlist.remove(currentSong);
        Collections.shuffle(songlist);
        songlist.addFirst(currentSong);
        currIndex = 0;
    }
     
    public void createSortedQueue(File currentSong) {
        songlist.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        currIndex = songlist.indexOf(currentSong);
    }
     
    public void printAll() {
        tableView.getItems().clear();;
        for (File f : songlist) {
            System.out.println(f.getName());
            tableView.getItems().add(f)
          
        }
    }
    
    public void save(File saveFile) {
        System.out.println("Saving playlist to " + saveFile.getAbsolutePath());
    }

    public File getPlaylistFile() {
        return playlistFile;
    }

}
