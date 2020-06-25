package mediaplayer;

import java.net.URI;

/**
 * Track
 */
public class SongTrack {

    // Track name
    private String trackName;

    // Path to file
    private URI trackPath;

    // Default constructor. Important for serialization
    public SongTrack() {

    }

    SongTrack(String trackName, URI trackPath) {

        this.trackName = trackName;
        this.trackPath = trackPath;
    }

    public String getTrackName() {
        return trackName;
    }

    public URI getTrackPath() {
        return trackPath;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public void setTrackPath(URI trackPath) {
        this.trackPath = trackPath;
    }

}