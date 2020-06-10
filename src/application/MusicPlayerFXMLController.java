package application;

import playlist.PlaylistManager;
import playlist.Song;
import util.Observer;
import util.ObservableObject;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import playlist.RowContextMenu;

/**
 * FXML Controller class
 *
 * @author sharv
 */
public class MusicPlayerFXMLController implements Initializable, Observer {

    
    String VERSION = "2.1";
    
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
    @FXML
    private TitledPane playlistTitlePane;
    @FXML
    private Accordion playlistAccordian;
    @FXML
    private TableView<Song> queueTable;
    @FXML
    private TableView<Song> playlistTable;
    @FXML
    private TextField playlistSearchBox;
    
    private Scene scene;
    private Stage stage;
    private Song currentSong;
    private MediaPlayer player;
    private Media songMedia;
    private PlayerSpectrumListener spectrumListener;
    private PlaylistManager playlistManager;

    private Image defaultImage;

    private Duration songDuration;
    private boolean loop;
    private boolean isPlaying;
    private double xOffset;
    private double yOffset;
  
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        xOffset = 0;
        yOffset = 0;
        
        isPlaying = false;
        
        if (darkTheme.isSelected())
            defaultImage = new Image(getClass().getClassLoader().getResourceAsStream("defaultArtDark.png"));
        else
            defaultImage = new Image(getClass().getClassLoader().getResourceAsStream("defaultArtLight.png"));

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

        playlistTitlePane.heightProperty().addListener(new ChangeListener<Number>()  {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {

                if (stage == null)
                    return;
                
                if (oldVal.doubleValue() == 0) {
                    // Init
                    stage.setHeight(340);
                } else {
                    stage.setHeight(stage.getHeight() + (newVal.doubleValue() - oldVal.doubleValue()));
                }
            }
        });     
        
        playlistTable.getColumns().clear();
        queueTable.getColumns().clear();
        playlistAccordian.setDisable(true);
        
        titleLabel.setFont(Font.loadFont(getClass().getClassLoader().getResourceAsStream("joystix_monospace.ttf"), 44));
    }
    
    @Override
    public void update(ObservableObject observable) {
        
        if (!currentSong.checkAll()) {
            songNameLabel.setText(currentSong.getTitle());
        } else {
            songNameLabel.setText(String.format("[%s] %s - %s", currentSong.getAlbum(), currentSong.getArtist(), currentSong.getTitle()));
            updateAlbumArtImage(currentSong.getAlbumArt());
        }
    }

    public void setStage(Stage newStage) {
        this.stage = newStage;
        stage.getIcons().add(defaultImage);
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
            defaultImage = new Image(getClass().getClassLoader().getResourceAsStream("defaultArtDark.png"));
            scene.getStylesheets().add(getClass().getClassLoader().getResource("dark_theme.css").toString());
        } else {
            defaultImage = new Image(getClass().getClassLoader().getResourceAsStream("defaultArtLight.png"));
            scene.getStylesheets().add(getClass().getClassLoader().getResource("light_theme.css").toString());
        }
        
        if (currentSong == null || currentSong.getAlbumArt() == null)
        {
            updateAlbumArtImage(defaultImage);
            stage.getIcons().clear();
            stage.getIcons().add(defaultImage);
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
            
            updateAlbumArtImage(defaultImage);
            currentSong.setSongMedia(songMedia);
            currentSong.processMetadata();
        }
    }

    private void updatePlayer() {
        
        if (player == null || spectrumListener == null)
            return;
        
        player.setVolume(volumeSlider.getValue());
        spectrumListener.setVolume(volumeSlider.getValue());
    }
    
    private void initPlaylistTable(TableView<Song> table) {
        
        table.getColumns().clear();

        TableColumn<Song, String> title = new TableColumn("Title");
        TableColumn<Song, String> artist = new TableColumn("Artist");
        TableColumn<Song, String> album = new TableColumn("Album");
        TableColumn<Song, String> genre = new TableColumn("Genre");
        TableColumn<Song, String> year = new TableColumn("Year");
        
        title.setCellValueFactory (new PropertyValueFactory<>("title"));
        artist.setCellValueFactory (new PropertyValueFactory<>("artist"));
        album.setCellValueFactory (new PropertyValueFactory<>("album"));
        genre.setCellValueFactory (new PropertyValueFactory<>("genre"));
        year.setCellValueFactory (new PropertyValueFactory<>("year"));
        
        table.getColumns().addAll(title, artist, album, genre, year);
        
        for (int i = 0; i < table.getColumns().size(); i++) {
            customizeFactory((TableColumn<Song, String>) table.getColumns().get(i));
        }
        
        table.setRowFactory(tableView -> {
            TableRow<Song> row = new TableRow();
            
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    currentSong = row.getItem();
                    currentSong.addObserver(this);
                    
                    if (playlistManager != null)
                        playlistManager.setCurrentSong(currentSong);
                    
                    playSong(true);
                    updatePanel();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    RowContextMenu rcm = new RowContextMenu(this, row.getItem());
                    rcm.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            
            return row;
        });
        
        table.setOnKeyPressed(event -> {
            if (table.getSelectionModel().isEmpty())
                return;
            
            switch (event.getCode()) {
                case DELETE:
                    int i = table.getSelectionModel().getSelectedIndex();
                    deleteSong(table.getSelectionModel().getSelectedItem());
                    table.getSelectionModel().select(i);
                    break;
                case Q:
                    queueSong(table.getSelectionModel().getSelectedItem());
                    break;
                case SPACE:
                    toggleSong(null);
                    break;
                default:
                    break;
            }
        });
    }

    private void customizeFactory(TableColumn<Song, String> column) {
        column.setCellFactory(col -> {
            return new TableCell<Song, String>() {
               @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item);
                        Song song = getTableView().getItems().get(getIndex());
                        if (song == currentSong) {
                            setStyle("-fx-background-color: rgb(255, 120, 120)");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
        });       
    }
    
    private void createNewPlaylist(File playlistFile) {
        initPlaylistTable(playlistTable);
        initPlaylistTable(queueTable);

        playlistTable.setPlaceholder(new Label("No music found in playlist"));
        queueTable.setPlaceholder(new Label("No music in queue"));
        
        playlistSearchBox.textProperty().addListener((obs, oldVal, newVal) -> {
            if (playlistManager == null)
                return;
            
            FilteredList<Song> filteredList = new FilteredList<>(playlistManager.getSongList(), b -> true);
            filteredList.setPredicate(song -> {
                
                if (newVal == null || newVal.isEmpty())
                    return true;
                
                String lowerCaseQuery = newVal.toLowerCase();
                
                return ((Song)song).getArtist().toLowerCase().contains(lowerCaseQuery) || 
                        ((Song)song).getAlbum().toLowerCase().contains(lowerCaseQuery) ||
                        ((Song)song).getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        ((Song)song).getGenre().toLowerCase().contains(lowerCaseQuery) ||
                        ((Song)song).getYear().toLowerCase().contains(lowerCaseQuery);
            });
            
            SortedList<Song> sortedQuery = new SortedList<>(filteredList);
            
            playlistTable.getItems().clear();
            for (Song s : sortedQuery)
                playlistTable.getItems().add(s);
        });
        
        playlistManager = new PlaylistManager(playlistFile, playlistTable, queueTable);
        playlistAccordian.setDisable(false);
        playlistTitlePane.expandedProperty().setValue(true);
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

        songMedia = new Media(currentSong.getSongFile().toURI().toString());
        player = new MediaPlayer(songMedia);
        
        player.currentTimeProperty().addListener(new InvalidationListener() 
        {
            public void invalidated(Observable ov) {
                updateDuration();
            }
        });
        
        player.setOnReady(new Runnable() {
            public void run() {
                if (player == null)
                    return;
                
                songDuration = player.getMedia().getDuration();
                updatePlayer();
            }
        });
        
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (loop) {
                    player.seek(Duration.ZERO);
                    player.play();
                }
                else {
                    if (playlistManager != null) {
                        nextSong(null);
                    } else {
                        stopSong(null);
                    }
                }
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
    
    public void deleteSong(Song deletedSong) {
        playlistManager.deleteSong(deletedSong, deletedSong == currentSong);
        spectrumListener.clearCanvas();
    } 
    
    public void queueSong(Song queuedSong) {
        
        if (playlistManager != null)
            playlistManager.queueSong(queuedSong);
    }
    
    public void removeSongFromQueue(Song queuedSong) {
        
        if (playlistManager != null)
            playlistManager.removeSongFromQueue(queuedSong);
    }
    
    private void showNoPlaylistError() {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Add to Playlist");
//        alert.setHeaderText("No existing playlist.");
//        alert.setContentText("You have not created a playlist. Do you wish to create one?");
//        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(defaultImage);
//
//        DialogPane dialogPane = alert.getDialogPane();
//        dialogPane.getStylesheets().clear();
//
//        if (darkTheme.isSelected()) {
//            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("dark_theme.css").toString());
//        } else {
//            dialogPane.getStylesheets().add(getClass().getClassLoader().getResource("light_theme.css").toString());
//        }
//
//        dialogPane.getStyleClass().add("dialogue-pane");
//
//        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
//        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
//        alert.getButtonTypes().setAll(okButton, noButton);
//        alert.showAndWait().ifPresent(type -> {
//                if (type.getButtonData() == ButtonBar.ButtonData.YES) {
//                    createNewPlaylist(null);
//                } else {
//                    return;
//                } 
//        });
//        
        createNewPlaylist(null);
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
        
        if (player == null || playlistManager == null)
            return;
        
        Song s = playlistManager.getNextSong();
        
        if (s == null)
            return;
        
        currentSong = s;
        currentSong.addObserver(this);
        playSong(true);
        updatePanel();
    }

    @FXML
    private void previousSong(ActionEvent event) {
        
        if (player == null || playlistManager == null)
            return;
        
        if (songSlider.getValue() > 0.05 || playlistManager == null) {
            
            // Replay song
            player.seek(Duration.ZERO);
            player.play();
            
        } else {
            // Previous song in playlist
            Song s = playlistManager.getPreviousSong();
            
            if (s == null)
                return;
            
            currentSong = s;
            currentSong.addObserver(this);
            playSong(true);
            updatePanel();
        }
    }

    @FXML
    private void selectSong(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();

        if (currentSong != null) {
            System.out.println(currentSong.getSongFile().getParentFile());
            fileChooser.setInitialDirectory(currentSong.getSongFile().getParentFile());
        }

        fileChooser.setTitle("Open File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio File (*.mp3, *.wav)", "*.mp3", "*.wav")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            currentSong = new Song(selectedFile);
            currentSong.addObserver(this);
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
        
        if (playlistManager == null)
            return;
        
        if (shuffleToggle.isSelected())
            playlistManager.createShuffledQueue(currentSong);
        else
            playlistManager.createSortedQueue(currentSong);
        
        playlistManager.printAll();
        playlistManager.setShuffle(shuffleToggle.isSelected());
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
        alert.setHeaderText("Tunez v"+VERSION);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(defaultImage);

        FlowPane pane = new FlowPane();
        VBox vbox = new VBox();
        vbox.setSpacing(7);
        Label label = new Label("Tunez is a minimalistic media player implemented in Java using JavaFX and FXML documents.\n\nMore information can be found here:");
        Hyperlink link = new Hyperlink("https://github.com/sharvenp/Tunez");
        link.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        Label label2 = new Label("© sharvenp All Rights Reserved");
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

    @FXML
    private void selectPlaylist(ActionEvent event) {
        
        FileChooser fileChooser = new FileChooser();

        if (playlistManager != null && playlistManager.getPlaylistFile() != null) {
            fileChooser.setInitialDirectory(playlistManager.getPlaylistFile().getParentFile());
        }

        fileChooser.setTitle("Open Playlist");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tunez Playlist (*.tunez)", "*.tunez")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            createNewPlaylist(selectedFile);
        }
    }
    
    @FXML
    private void savePlaylist(ActionEvent event) {
        
        if (playlistManager == null) {
            showNoPlaylistError();
        }

        if (playlistManager == null)
            return;
        
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Save Playlist");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tunez Playlist (*.tunez)", "*.tunez")
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            playlistManager.save(selectedFile);
        }
    }

    @FXML
    private void createPlaylist(ActionEvent event) {
        createNewPlaylist(null);
    }

    @FXML
    private void addCurrentToPlaylist(ActionEvent event) {
        
        if (currentSong == null)
            return;
        
        if (playlistManager == null)
           showNoPlaylistError();
        
        if (playlistManager == null)
            return;
        
        playlistManager.addSong(currentSong);
        System.out.println("Added " + currentSong.getTitle());
    }

    @FXML
    private void addToPlaylist(ActionEvent event) {
        
        if (playlistManager == null)
            showNoPlaylistError();
        
        if (playlistManager == null)
            return;
        
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Add to Playlist");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio File (*.mp3, *.wav)", "*.mp3", "*.wav")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            playlistManager.addSongs(selectedFiles);
            
            if (currentSong == null) {
                currentSong = playlistManager.getCurrentSong();
                currentSong.addObserver(this);
                playSong(true);
                updatePanel();
            }
            
            System.out.println("Added " + selectedFiles.size() + " songs.");
        }
    }
}
