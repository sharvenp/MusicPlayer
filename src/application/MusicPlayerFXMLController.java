package application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
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
    private Label titleLabel;
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
    @FXML
    private Canvas spectrumCanvas;
    @FXML
    private CheckMenuItem audioSpectrum;
    @FXML
    private CheckMenuItem darkTheme;
    
    private Scene scene;
    private Stage stage;
    private File currentSong;
    private MediaPlayer player;
    private Media song;
    private PlayerSpectrumListener spectrumListener;
    private String title;
    private String artist;
    private String album;
    private Image albumArtImage;
    private Image defaultImage;

    private Duration songDuration;
    private boolean loop;
    private boolean shuffle;
    private boolean isPlaying;
    private double xOffset;
    private double yOffset;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        xOffset = 0;
        yOffset = 0;
        
        isPlaying = false;
        defaultImage = new Image(getClass().getClassLoader().getResourceAsStream("defaultArt.png"));
        
        updatePanel();

        volumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
           
            double volumeSliderValue = volumeSlider.getValue();

            if (volumeSliderValue == 0.0) {
                volumeLabel.setText("🔇");
            } else if (volumeSliderValue > 0.0 && volumeSliderValue <= 0.5) {
                volumeLabel.setText("🔉");
            } else {
                volumeLabel.setText("🔊");
            }
            updatePlayer();
        });
        
        songSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                if (!isPlaying && player != null) {
                    player.seek(songDuration.multiply(songSlider.getValue()));
                }
            }
        });
        
        titleLabel.setFont(Font.loadFont(getClass().getClassLoader().getResourceAsStream("joystix_monospace.ttf"), 44));
    }

    public void setStage(Stage newStage) {
        this.stage = newStage;
    }
    
    public void setScene(Scene newScene) {
        this.scene = newScene;
    }
    
    private void updateAlbumArtImage(Image newImage) {
        albumImageView.setImage(newImage);
    }

    public void updateTheme() {
        scene.getStylesheets().clear();
        
        if (darkTheme.isSelected()) {
            scene.getStylesheets().add(getClass().getClassLoader().getResource("dark_theme.css").toString());
        } else {
            scene.getStylesheets().add(getClass().getClassLoader().getResource("light_theme.css").toString());
        }
    }
    
    private void updatePanel() {

        songSlider.setValue(0);

        if (currentSong == null) {
            
            songNameLabel.setText("...");
            songSlider.setMax(0);
            timeLabel.setText("--:--/--:--");
            playButton.setText("▶");
            
            updateAlbumArtImage(defaultImage);
            
        } else {
            
            title = "";
            artist = "";
            album = "";
            
            updateAlbumArtImage(defaultImage);
            
            String filteredName = currentSong.getName().replace(".mp3", "");
            filteredName = filteredName.replace(".wav", "");
            songNameLabel.setText(filteredName);
            
            song.getMetadata().addListener((MapChangeListener<String, Object>) c -> {
                if (c.wasAdded() && c.getValueAdded() != null) {
                    
                    if ("raw metadata".equals(c.getKey()))
                        return;
                    
                    if ("artist".equals(c.getKey())) {
                        artist = c.getValueAdded().toString();
                    } else if ("title".equals(c.getKey())) {
                        title = c.getValueAdded().toString();
                    } else if ("album".equals(c.getKey())) {
                        album = c.getValueAdded().toString();
                    } else if ("image".equals(c.getKey()) && c.getValueAdded() != null) {
                        albumArtImage = (Image)c.getValueAdded();
                        updateAlbumArtImage((Image)c.getValueAdded());
                    }
                    
                    if (!artist.equals("") && !artist.equals("") && !artist.equals(""))
                        songNameLabel.setText(String.format("[%s] %s - %s", album, artist, title));
                }
            });
        }
    }

    private void updatePlayer() {
        
        if (player == null || spectrumListener == null)
            return;
        
        player.setVolume(volumeSlider.getValue());
        spectrumListener.setVolume(volumeSlider.getValue());
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
        
        if (player == null) {
            return;
        }
        
        Platform.runLater(new Runnable() {
           public void run() {
             
                Duration currentTime = player.getCurrentTime();
                timeLabel.setText(formatTime(currentTime));
                
                songSlider.setDisable(songDuration.isUnknown());

                if (!songSlider.isDisabled() && 
                    songDuration.greaterThan(Duration.ZERO) && 
                    !songSlider.isValueChanging()) {
                    songSlider.setValue(currentTime.toMillis() / songDuration.toMillis());
                }
           }
        });
    }
    
    private void playSong(boolean firstTime) {

        isPlaying = true;
        
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
                songDuration = player.getMedia().getDuration();
                updatePlayer();
            }
        });
        
        spectrumListener = new PlayerSpectrumListener(spectrumCanvas, player);
        spectrumListener.setEnable(audioSpectrum.isSelected());
        player.setAudioSpectrumListener(spectrumListener);
        player.setAudioSpectrumNumBands(64);
        player.setAudioSpectrumInterval(0.001);
        player.setAudioSpectrumThreshold(-75);
        
        songSlider.setMax(1.0);
        
        player.play();
        playButton.setText("⏸");
    }

    @FXML
    private void toggleSong(ActionEvent event) {

        if (currentSong == null) {
            return;
        }

        if (isPlaying) {
            // Pause
            playButton.setText("▶");
            player.pause();
            isPlaying = false;
            
            spectrumListener.setSkip(true);
            if (spectrumListener != null)
                spectrumListener.clearCanvas();
            
        } else {
            // Play
            playButton.setText("⏸");
            spectrumListener.setSkip(false);
            playSong(player.getCurrentTime().equals(Duration.ZERO));
        }
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
        if (songSlider.getValue() < 0.05) {
            
            // Previous song in playlist
            
        } else {
            // Replay song
            player.seek(Duration.ZERO);
            player.play();
        }
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
        
        if (player == null)
            return;
        
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

    @FXML
    private void toggleShuffle(ActionEvent event) {
        shuffle = shuffleToggle.isSelected();
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void releasedOnSongSlider(MouseEvent event) {
        if (player == null)
            return;
        
        isPlaying = true;
        player.play();
        spectrumListener.setSkip(false);
        
        updatePlayer();
    }

    @FXML
    private void pressedOnSongSlider(MouseEvent event) {
        if (player == null)
            return;
        
        isPlaying = false;
        player.pause();
        spectrumListener.setSkip(true);
        
        songSlider.setValue((double) event.getX() / (double) songSlider.getWidth());
        
        player.seek(songDuration.multiply(songSlider.getValue()));
    }

    @FXML
    private void toggleAudioSpectrum(ActionEvent event) {
        if (spectrumListener != null)
            spectrumListener.setEnable(audioSpectrum.isSelected());
    }

    @FXML
    private void toggleDarkTheme(ActionEvent event) {
        updateTheme();
    }

    @FXML
    private void minimizeApplication(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML
    private void pressedOnPane(MouseEvent event) {
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }
    
    @FXML
    private void dragPane(MouseEvent event) {
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }

    @FXML
    private void openManual(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText("Still working on it! - Sharven");
        alert.setTitle("User Manual");
        alert.setHeaderText(null);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(defaultImage);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();

        if (darkTheme.isSelected()) {
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("dark_theme.css").toString());
        } else {
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("light_theme.css").toString());
        }

        dialogPane.getStyleClass().add("dialogue-pane");
        alert.showAndWait();
    }

    @FXML
    private void openAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Tunez v1.2");
        
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(defaultImage);

        FlowPane pane = new FlowPane();
        VBox vbox = new VBox();
        vbox.setSpacing(7);
        Label label = new Label("Tunez is a minimalistic media player implemented in Java using JavaFX and FXML documents.\n\nMore information can be found here:");
        Hyperlink link = new Hyperlink("https://github.com/sharvenp/Tunez");
        link.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        Label label2 = new Label("© 2020 sharvenp All Rights Reserved");
        vbox.getChildren().addAll(label, link, label2);
        pane.getChildren().add(vbox);

        link.setOnAction((e) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/sharvenp/Tunez"));
                alert.close();
            } catch (Exception exc) {
                System.out.println("Could not connect.");
            }
        });
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.contentProperty().set(pane);
        dialogPane.getStylesheets().clear();

        if (darkTheme.isSelected()) {
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("dark_theme.css").toString());
        } else {
            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("light_theme.css").toString());
        }

        dialogPane.getStyleClass().add("dialogue-pane");
        
        alert.showAndWait();
    }
}
