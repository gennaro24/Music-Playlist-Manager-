package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryPlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.InMemoryTrackRepository;

/**
 * Stub manuale di PlaybackService posizionato nel layer dei test applicativi.
 * Soddisfa la dipendenza di PlaylistRepository evitando il NullPointerException.
 */
public class FakePlaybackService extends PlaybackService {
    
    private boolean handleDeletedTrackCalled = false;
    private String lastDeletedTrackId = null;
    
    private boolean handleDeletedPlaylistCalled = false;
    private String lastDeletedPlaylistId = null;

    public FakePlaybackService() {
        // Passiamo un'istanza reale in memoria per superare il controllo Objects.requireNonNull
        super(new InMemoryPlaylistRepository(new InMemoryTrackRepository())); 
    }

    @Override
    public void handleDeletedTrack(String trackId) {
        this.handleDeletedTrackCalled = true;
        this.lastDeletedTrackId = trackId;
    }

    @Override
    public void handleDeletedPlaylist(String playlistId) {
        this.handleDeletedPlaylistCalled = true;
        this.lastDeletedPlaylistId = playlistId;
    }
    
    @Override
    public PlaybackSnapshot getSnapshot() {
        return null;
    }

    // Metodi getter per le asserzioni JUnit
    public boolean isHandleDeletedTrackCalled() {
        return handleDeletedTrackCalled;
    }

    public String getLastDeletedTrackId() {
        return lastDeletedTrackId;
    }

    public boolean isHandleDeletedPlaylistCalled() {
        return handleDeletedPlaylistCalled;
    }

    public String getLastDeletedPlaylistId() {
        return lastDeletedPlaylistId;
    }
}