package it.unisa.sad.playlistmanager.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestisce la coda di riproduzione corrente, mantenendo traccia della lista dei brani,
 * dell'indice del brano attualmente in riproduzione e della sorgente.
 */
public class PlaybackQueue {
    
    private final List<Track> tracks;
    private int currentIndex;
    private final PlaybackSource source;

    /**
     * Inizializza una nuova coda di riproduzione.
     * * @param tracks Lista delle tracce da riprodurre.
     * @param source La sorgente della riproduzione (es. PLAYLIST, CATALOG).
     */
    public PlaybackQueue(List<Track> tracks, PlaybackSource source) {
        if (tracks == null) {
            this.tracks = new ArrayList<>();
        } else {
            this.tracks = new ArrayList<>(tracks); // Copia difensiva
        }
        this.source = source;
        this.currentIndex = this.tracks.isEmpty() ? -1 : 0;
    }

    /**
     * @return La lista immutabile delle tracce nella coda.
     */
    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    /**
     * @return La sorgente di riproduzione corrente.
     */
    public PlaybackSource getSource() {
        return source;
    }

    /**
     * @return L'indice della traccia correntemente in riproduzione.
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Imposta un nuovo indice corrente (utile per lo skip o la selezione manuale).
     * @param index Il nuovo indice da impostare.
     * @throws IndexOutOfBoundsException Se l'indice non è valido.
     */
    public void setCurrentIndex(int index) {
        if (index < 0 || index >= tracks.size()) {
            throw new IndexOutOfBoundsException("Indice traccia non valido per la coda corrente.");
        }
        this.currentIndex = index;
    }

    /**
     * @return La traccia corrente, o null se la coda è vuota.
     */
    public Track getCurrentTrack() {
        if (tracks.isEmpty() || currentIndex < 0 || currentIndex >= tracks.size()) {
            return null;
        }
        return tracks.get(currentIndex);
    }
    
    /**
     * @return true se ci sono tracce nella coda, false altrimenti.
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }
}