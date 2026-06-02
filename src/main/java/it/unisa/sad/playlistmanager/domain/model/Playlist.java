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
     * Impedisce l'inserimento di tracce nulle o duplicate.
     *
     * @param track La traccia validata da inserire.
     * @throws IllegalArgumentException Se la traccia è nulla o già presente nella playlist.
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una traccia nulla alla playlist.");
        }
        
        for (Track t : this.tracks) {
            if (t.getId() != null && t.getId().equals(track.getId())) {
                throw new IllegalArgumentException("La traccia '" + track.getTitle() + "' è già presente in questa playlist.");
            }
        }
        
        this.tracks.add(track);
    }

    /**
     * Metodo di business per rimuovere una traccia dalla playlist a partire dal suo ID.
     * Scorre l'elenco interno e rimuove l'elemento corrispondente preservando l'ordine dei rimanenti.
     *
     * @param trackId L'identificativo univoco della traccia da rimuovere.
     * @throws IllegalArgumentException Se l'ID fornito è nullo, vuoto o se la traccia non è presente nella playlist.
     */
    public void removeTrack(String trackId) {
        if (trackId == null || trackId.trim().isEmpty()) {
            throw new IllegalArgumentException("Impossibile rimuovere una traccia fornendo un ID nullo o vuoto.");
        }

        boolean removed = false;
        // Utilizziamo un iteratore per evitare ConcurrentModificationException in logiche complesse
        java.util.Iterator<Track> iterator = this.tracks.iterator();
        while (iterator.hasNext()) {
            Track currentTrack = iterator.next();
            if (currentTrack.getId() != null && currentTrack.getId().equals(trackId)) {
                iterator.remove();
                removed = true;
                break; // ID univoco trovato, interrompiamo il ciclo
            }
        }

        if (!removed) {
            throw new IllegalArgumentException("Errore: La traccia con ID '" + trackId + "' non è presente in questa playlist.");
        }
    }
}