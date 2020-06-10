/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package playlist;

import java.io.File;
import javafx.collections.MapChangeListener;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import util.ObservableObject;

/**
 *
 * @author sharv
 */
public class Song extends ObservableObject {
    
    private File songFile;

    private String title;
    private String artist;
    private String album;
    private String year;
    private String genre;
    private Image albumArtImage;
    
    private Media songMedia;
    private boolean processedMetadata;
    
    public Song(File newFile) {
        super();
        processedMetadata = false;
        songFile = newFile;
    }
    
    public void setSongMedia(Media newMedia) {
        songMedia = newMedia;
    }
    
    public void processMetadata() {

        if (processedMetadata) {
            notifyObservers();
            return;
        }
        
        placeHolderAll();
        notifyObservers();
        
        songMedia.getMetadata().addListener((MapChangeListener<String, Object>) c -> {
            if (c.wasAdded() && c.getValueAdded() != null) {
                
                if ("raw metadata".equals(c.getKey()))
                    return;

                if ("artist".equals(c.getKey())) {
                    artist = c.getValueAdded().toString();
                } else if ("title".equals(c.getKey())) {
                    title = c.getValueAdded().toString();
                } else if ("album".equals(c.getKey())) {
                    album = c.getValueAdded().toString();
                } else if ("year".equals(c.getKey())) {
                    year = c.getValueAdded().toString();
                } else if ("genre".equals(c.getKey())) {
                    genre = c.getValueAdded().toString();
                } else if ("image".equals(c.getKey())) {
                    albumArtImage = (Image)c.getValueAdded();
                }
                
                if (checkAll() && !processedMetadata)
                {
                    processedMetadata = true;
                    notifyObservers();
                }
            }
        });
    }
    
    public void placeHolderAll() {
        
        String filteredName = songFile.getName().replace(".mp3", "");
        filteredName = filteredName.replace(".wav", "");
        
        title = filteredName;
        artist = "-";
        album = "-";
        genre = "-";
        year = "-";
        albumArtImage = null;
    }
    
    public boolean checkAll() {
        return !artist.equals("-") && !album.equals("-") && !title.equals("-") && albumArtImage != null;
    }
    
    public File getSongFile() {
        return songFile;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getArtist() {
        return artist;
    }
    
    public String getAlbum() {
        return album;
    }
    
    public Image getAlbumArt() {
        return albumArtImage;
    }
    
    public String getYear() {
        return year;
    }
    
    public String getGenre() {
        return genre;
    }
}
