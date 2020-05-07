package application;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
    private Label timeLabel;

    private Stage stage;
    private String songDirectory;
    private String songName;
    private File currentSong;
    private MediaPlayer player;

    private Duration songDuration;
    private boolean loop;
    private boolean shuffle;
    private boolean isPlaying;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        updatePanel();
        isPlaying = false;

        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updatePlayer();
        });

    }

    public void setStage(Stage newStage) {
        stage = newStage;
    }

    private void updatePanel() {

        songSlider.setValue(0);

        if (currentSong == null) {
            songDirectory = "";
            songName = "";
            songNameLabel.setText("");
            songSlider.setMax(0);
            timeLabel.setText("--:--/--:--");
        }

        System.out.println(songDirectory);
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

        double durationSeconds = songDuration.toSeconds();
        
        return "--:--/--:--";
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

        Media media;
        media = new Media(currentSong.toURI().toString());
        player = new MediaPlayer(media);
        
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
    }

    @FXML
    private void toggleSong(ActionEvent event) {

        if (currentSong == null) {
            return;
        }

        if (isPlaying) {
            // Pause
            player.pause();
        } else {
            // Play
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
                new FileChooser.ExtensionFilter("MP3 Files (*.mp3)", "*.mp3"),
                new FileChooser.ExtensionFilter("WAV Files (*.wav)", "*.wav")
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
}
