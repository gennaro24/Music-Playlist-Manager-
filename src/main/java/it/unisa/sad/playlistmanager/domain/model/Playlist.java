package it.unisa.sad.playlistmanager.domain.model;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entità del Modello di Dominio che rappresenta una Playlist musicale.
 * Incapsula lo stato e le regole di business, auto-validandosi.
 * @version 1.0
 */
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
