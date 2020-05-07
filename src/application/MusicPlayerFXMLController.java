package application;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author sharv
 */
public class MusicPlayerFXMLController implements Initializable {

    @FXML
    private Slider songSlider;
    @FXML
    private Label songNameLabel;
    @FXML
    private CheckBox loopToggle;
    @FXML
    private CheckBox shuffleToggle;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label volumeLabel;
    @FXML
    private Button playButton;
    @FXML
    private Label timeLabel;
    @FXML
    private ImageView albumImageView;
    
    private Stage stage;
    private File currentSong;
    private MediaPlayer player;
    private Media song;
    private String title;
    private String artist;
    private String album;


    private Duration songDuration;
    private boolean loop;
    private boolean shuffle;
    private boolean isPlaying;

    

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        updatePanel();
        isPlaying = false;

        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
           
            double volumeSliderValue = volumeSlider.getValue();

            if (volumeSliderValue == 0.0) {
                volumeLabel.setText("🔈");
            } else if (volumeSliderValue > 0.0 && volumeSliderValue <= 0.5) {
                volumeLabel.setText("🔉");
            } else {
                volumeLabel.setText("🔊");
            }

            
            updatePlayer();
        });
        
    }

    public void setStage(Stage newStage) {
        stage = newStage;
    }

    private void updatePanel() {

        songSlider.setValue(0);

        if (currentSong == null) {
            songNameLabel.setText("...");
            songSlider.setMax(0);
            timeLabel.setText("--:--/--:--");
            playButton.setText("▶");
        } else {
            songNameLabel.setText(currentSong.getName());
            song.getMetadata().addListener((MapChangeListener<String, Object>) c -> {
                if (c.wasAdded()) {
                    if ("artist".equals(c.getKey())) {
                        artist = c.getValueAdded().toString();
                    } else if ("title".equals(c.getKey())) {
                        title = c.getValueAdded().toString();
                    } else if ("album".equals(c.getKey())) {
                        album = c.getValueAdded().toString();
                    } else if ("image".equals(c.getKey())) {
                        albumImageView.setImage((Image)c.getValueAdded());
                        System.out.println("found");
                    }
                    songNameLabel.setText(String.format("[%s] %s - %s", album, artist, title));
                }
            });
        }
    }

    private void updatePlayer() {
        
        if (player == null)
            return;
        
        player.setVolume(volumeSlider.getValue());
        
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (loop) 
                {
                    player.seek(Duration.ZERO);
                    player.play();
                }
                else {
                    player.stop();
                    isPlaying = false;
                }
                
            }
        });
    }
    
    private String formatTime(Duration currentTime) {

        int durationSeconds = Math.round((float)songDuration.toSeconds());
        String durationMinuteString = String.format("%02d", durationSeconds / 60);
        String durationSecondString = String.format("%02d", durationSeconds % 60);
        
        int currentSeconds = Math.round((float)currentTime.toSeconds());
        String currentMinuteString = String.format("%02d", currentSeconds / 60);
        String currentSecondString = String.format("%02d", currentSeconds % 60);
        
        return currentMinuteString+":"+currentSecondString+"/"+durationMinuteString+":"+durationSecondString;
    }
    
    private void updateDuration() {
        
        Platform.runLater(new Runnable() {
           public void run() {
             
                if (player == null) {
                    return;
                }
               
                Duration currentTime = player.getCurrentTime();
                timeLabel.setText(formatTime(currentTime));

                if (songDuration.greaterThan(Duration.ZERO) && !songSlider.isValueChanging()) {
                    songSlider.setValue(currentTime.toMillis() / songDuration.toMillis());
                }
           }
        });
    }
    
    private void playSong(boolean firstTime) {

        if (!firstTime) {
            player.play();
            return;
        }
        
        if (player != null) {
            player.stop();
        }

        song = new Media(currentSong.toURI().toString());
        player = new MediaPlayer(song);
        
        player.currentTimeProperty().addListener(new InvalidationListener() 
        {
            public void invalidated(Observable ov) {
                updateDuration();
            }
        });
        
        player.setOnReady(new Runnable() {
            public void run() {
                updatePlayer();
                songDuration = player.getMedia().getDuration();
            }
        });
        
        songSlider.setMax(1);
        
        player.play();
        playButton.setText("⏸");
        isPlaying = true;
    }

    @FXML
    private void toggleSong(ActionEvent event) {

        if (currentSong == null) {
            return;
        }

        if (isPlaying) {
            // Pause
            playButton.setText("►");
            player.pause();
        } else {
            // Play
            playButton.setText("⏸");
            playSong(player.getCurrentTime().equals(Duration.ZERO));
        }

        isPlaying = !isPlaying;
    }

    @FXML
    private void stopSong(ActionEvent event) {
        
        if (player != null && currentSong != null) {
            player.stop();
            player.seek(Duration.ZERO);
            isPlaying = false;
            playButton.setText("▶");
            updateDuration();
        }
    }
    
    @FXML
    private void nextSong(ActionEvent event) {
        System.out.println("NEXT SONG");
    }

    @FXML
    private void previousSong(ActionEvent event) {
        System.out.println("PREVIOUS SONG");
    }

    @FXML
    private void selectSong(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();

        if (currentSong != null) {
            fileChooser.setInitialDirectory(currentSong.getParentFile());
        }

        fileChooser.setTitle("Select Song");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio File (*.mp3, *.wav)", "*.mp3", "*.wav")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            currentSong = selectedFile;
            playSong(true);
            updatePanel();
        }
    }

    @FXML
    private void toggleLoop(ActionEvent event) {
        loop = loopToggle.isSelected();
        updatePlayer();
    }

    @FXML
    private void toggleShuffle(ActionEvent event) {
        shuffle = shuffleToggle.isSelected();
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        Platform.exit();
    }
}
