package it.unisa.sad.playlistmanager.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private final String name;
    private final List<Track> tracks;

    public Playlist(String name) {
        this.name = name;
        this.tracks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Track> getTracks() {
        return tracks;
    }

}
