package it.unisa.sad.playlistmanager.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entità del Modello di Dominio che rappresenta una Playlist musicale.
 * Incapsula lo stato e le regole di business, auto-validandosi.
 * @version 1.0
 */
public class Playlist {
    private final String id;
    private String name;
    private final List<Track> tracks;

    /**
     * Costruttore completo dell'entità Playlist.
     * @param id   L'identificativo univoco. Se nullo o vuoto, viene generato automaticamente un UUID.
     * @param name Il nome assegnato alla playlist.
     * @throws IllegalArgumentException Se il nome viola le regole di validazione del dominio.
     */
    public Playlist(String id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto o nullo.");
        }
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.name = name;
        this.tracks = new ArrayList<>();

    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    
    /**
     * Ritorna una vista non modificabile della lista per preservare l'incapsulamento del dominio.
     * Impedisce modifiche esterne dirette alla collezione senza passare per i metodi di business.
     */
    public List<Track> getTracks() { 
        return Collections.unmodifiableList(tracks); 
    }

    // --- SETTERS ---
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto o nullo.");
        }
        this.name = name;
    }

    /**
     * Metodo di business per aggiungere una traccia alla playlist.
     * * @param track La traccia validata da inserire.
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una traccia nulla alla playlist.");
        }
        this.tracks.add(track);
    }
}