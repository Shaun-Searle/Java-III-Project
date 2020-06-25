package mediaplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;

public class PrimaryController {

    // Injections

    @FXML private ToggleButton btnPlay;

    @FXML private Button btnNext;

    @FXML private Button btnPrev;

    @FXML private Button btnSkipStart;

    @FXML private Button btnSkipEnd;

    @FXML private ListView<String> lstPlaylist;

    @FXML private Label lblTimer;

    @FXML private Slider sldVolume;

    @FXML private TextField txtSearch;

    // Inject for getting scene
    @FXML
    private AnchorPane mainWindow;


    // Mediaplayer
    private MediaPlayer mp;

    // Currently playing index rather tan using iterator
    int playingIndex = 0;

    // Backing linkedlist for the ui playlist
    LinkedList<SongTrack> playlist = new LinkedList<SongTrack>();


    // Init

    @FXML
    public void initialize() {

        // Plays song on double click
        lstPlaylist.setOnMouseClicked((e) ->  {
                if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {

                    var ind = lstPlaylist.getSelectionModel().getSelectedIndex();

                    if (ind >= 0) {
                       playTrack(ind); 
                    }
                    
                }
        });

        // Watches slider to set volume property
        sldVolume.valueProperty().addListener((observable, oldValue, newValue) -> { 
            if (mp != null) {
                mp.volumeProperty().set(newValue.doubleValue());
            }});

    }

    // Methods

    // Used for messageboxes
    @FXML
    private void alertUser(String msg, String header, String title) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait().ifPresent(rs -> {});
    }

    @FXML
    private void loadSongsFromFile() {
        // Loads songs from file dialog
        var f = fileDialog("Select MP3 Files", "MP3 Files", "*.mp3");

        if (f == null) { return; }

        // temp list to hold Tracks
        List<SongTrack> temp = new ArrayList<SongTrack>();

        // Create tracks from files
        f.forEach(e -> temp.add(new SongTrack(e.getName(), e.toURI())));

        var sorted = sortArray(temp);

        // Clear playlist before adding
        playlist.clear();

        // Add sorted tracks to playlist
        playlist.addAll(Arrays.asList(sorted));

        // Updates the ui list
        updatePlaylist(playlist);
    }

    // TODO Continue to make generic for use with mp3 and json loading
    // Opens an file dialog
    @FXML
    private List<File> fileDialog(String title, String filterName, String filter) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // For csv
        fileChooser.getExtensionFilters().add(new ExtensionFilter(filterName, filter));

        // Init to user dir
        File init = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(init);

        // Owned by main stage
        var f = fileChooser.showOpenMultipleDialog(mainWindow.getScene().getWindow());

        // if user selects nothing return
        if (f == null) {
            return null;
        }

        return f;
    }

    private SongTrack[] sortArray(List<SongTrack> temp) {

         // Store tracks in array to be sorted
         SongTrack[] sorted = new SongTrack[temp.size()];

         temp.toArray(sorted);
 
         // Sort the array
         quickSort(sorted);

         return sorted;
 
    }

    // Updates ui list
    private void updatePlaylist(List<SongTrack> f) {
        
        // Clear the ui list
        lstPlaylist.getItems().clear();

        // Trackname - md5 hash of file
        f.forEach(e -> lstPlaylist.getItems().add(e.getTrackName() + " - " + getHash(e.getTrackPath())));
    }

    // Get the md5 hash of file
    private String getHash(URI p) {
        
        String checksum = "";
        File file = new File(p);

        try (var fs = new FileInputStream(file);) {

            checksum = DigestUtils.md5Hex(IOUtils.toByteArray(fs));

        } catch (Exception e) {
            System.out.println("Could not get checksum of file! " + e);
        }

        return checksum;
    }

    // Play button handler
    @FXML
    private void play() {

        // Empty? DO nothing and not playing
        if (playlist.size() == 0) { btnPlay.setSelected(false); return; }

        // Attempt to play first track when player is null
        if (mp == null) { playTrack(0);}

        // Player status
        Status cur = mp.getStatus();

        if (cur == Status.PLAYING) {

            mp.pause();

        } else if (cur == Status.PAUSED || cur == Status.STOPPED) {

            mp.play();

        }

    }

    // Play track index and reset handlers
    @FXML
    private void playTrack(int t) {

        // Out of bounds? Do nothing
        if (t < 0 || t > playlist.size() - 1) { return; }

        if (mp != null) { mp.stop(); }

        Media song = new Media(playlist.get(t).getTrackPath().toString());
        mp = new MediaPlayer(song);

        // Attach new event handlers

        mp.currentTimeProperty().addListener((observable, oldTime, newTime) -> {

            String currentTime = DurationFormatUtils.formatDuration((long)newTime.toMillis(), "H:mm:ss");

            String totalDuration = DurationFormatUtils.formatDuration((long)mp.getTotalDuration().toMillis(), "H:mm:ss");

            lblTimer.setText(String.format("%s / %s", currentTime, totalDuration));

        });

        // Ensure the play button is in the correct state
        mp.setOnPaused(() -> { btnPlay.setSelected(false);});

        mp.setOnPlaying(() -> { btnPlay.setSelected(true);});

        // Manual autoplay
        mp.setOnEndOfMedia(() -> { playTrack(playingIndex + 1); });

        // Start playing
        mp.play();

        // Set the selected list item and playing index
        lstPlaylist.getSelectionModel().select(t);
        playingIndex = t;
    }

    // Controls that use playTrack to navigate 

    @FXML
    private void prevTrack() {
        playTrack(playingIndex - 1);
    }

    @FXML
    private void nextTrack() {
        playTrack(playingIndex + 1);
    }

    @FXML
    private void skipEnd() {
        playTrack(playlist.size() - 1);
    }

    @FXML
    private void skipStart() {
        playTrack(0);
    }

    // Sorting

    // Calls the recursion without having to supply start and end
    // Main entry
    public void quickSort(SongTrack[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    // Quicksort based on hoares partitioning
    private void quickSort(SongTrack arr[], int left, int right) {

        if (left >= right) {
            return;
        }

        int pivot = part(arr, left, right);

        // Sort array less than pivot
        quickSort(arr, left, pivot);

        // Sort greater than
        quickSort(arr, pivot + 1, right);
    }

    private int part(SongTrack arr[], int left, int right) {
        // Rightmost element as pivot
        SongTrack pivot = arr[left];
        int start = left - 1;
        int end = right + 1;

        while (true) {
            do {
                start++;
            } while (arr[start].getTrackName().compareToIgnoreCase(pivot.getTrackName()) < 0);

            do {
                end--;
            } while (arr[end].getTrackName().compareToIgnoreCase(pivot.getTrackName()) > 0);

            if (start >= end) {
                return end;
            }

            swap(arr, start, end);

        }
    }

    // Swaps elements
    private void swap(SongTrack array[], int a, int b) {
        SongTrack swap = array[a];
        array[a] = array[b];
        array[b] = swap;
    }

    // Search
    @FXML
    private void searchButton() {

        String input = "";

        input = txtSearch.getText();

        if (input.isEmpty()) { alertUser("Field must not be empty", "Error!", "Error!"); return; }

        int index = binarySearch(input);

        if (index < 0) { alertUser("Song not found. Be sure to only search for the title and extension!", "Not found!" , "Not found!"); return;}

        lstPlaylist.getSelectionModel().select(index);
    }

    // Using inbuilt with a custom comparator 
    public int binarySearch(String search) {

        Comparator<SongTrack> t = new Comparator<SongTrack>()
        {
            public int compare(SongTrack a, SongTrack b) {
                return a.getTrackName().compareTo(b.getTrackName());
            }
        };

        int index = Collections.binarySearch(playlist, new SongTrack(search, null), t);

        return index;
    }

    // Choose a file and save the playlist
    @FXML
    private void btnWriteJson() {

        try {

            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("Save JSON");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
    
            File init = new File(System.getProperty("user.dir"));
            fileChooser.setInitialDirectory(init);
    
            var f = fileChooser.showSaveDialog(mainWindow.getScene().getWindow());
    
            if (f == null) {return;}
    
            // Send tableView and selected path

            writeJson(f);
        } catch (Exception e) {
            System.out.println("Failed to write JSON: " + e);
        }

    }

    // Write the object to specified file
    private void writeJson(File f) throws IOException {
        
            final ObjectMapper mapper = new ObjectMapper();
        
            // Write
            mapper.writeValue(f, playlist);
    }

    // Load from json
    @FXML
    private void btnLoadJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load playlist from JSON");

        // For csv
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Load JSON", "*.json"));

        // Init to user dir
        File init = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(init);

        // Owned by main stage
        var f = fileChooser.showOpenDialog(mainWindow.getScene().getWindow());

        // if user selects nothing return
        if (f == null) {
            return;
        }


        try {

            loadJson(f);

        } catch (Exception e) {
            System.out.println("Error loading JSON: " + e);
        }

        
    }

    // Load serialized list of Tracks
    private void loadJson(File file) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();

        // As array to simplify type mapping
        var temp = mapper.readValue(file, SongTrack[].class);

        // Prevent empty lists from loading
        if (temp.length > 0) {

            var newList = Arrays.asList(temp);

            playlist.clear();
            playlist.addAll(newList);

            updatePlaylist(playlist);

        } else {
            return;
        }
    }

    // Open help menu
    @FXML
    private void openHelp() {

        try {

            var f = new File(this.getClass().getResource("help.html").getFile()).toURI();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(f);

                 System.out.println("Help Opened!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
