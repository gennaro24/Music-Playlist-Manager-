package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class PlaybackServiceTest {
// ===================================================================================
    // FAKE REPOSITORIES PER SIMULARE IL DATABASE
    // ===================================================================================

    /**
     * Sostituto finto (Fake Object) destinato all'isolamento dello stato delle playlist.
     * Implementa l'interfaccia contrattuale aggiornata del modulo persistence.
     */
    class FakePlaylistRepository implements PlaylistRepository {
        /** Interruttore per monitorare l'invocazione del salvataggio. */
        boolean isSaveCalled = false;
        /** Interruttore per catturare l'avvenuta associazione di una traccia. */
        boolean isAddTrackCalled = false;
        /** Interruttore per catturare l'avvenuta disassociazione di una traccia. */
        boolean isRemoveTrackCalled = false;
        /** Memorizza l'istanza dell'ultima playlist inviata alla persistenza. */
        Playlist savedPlaylist = null;

        /** Lista mutabile per la gestione delle tracce interne alla playlist finta 1. */
        List<Track> tracksInPlaylist1 = new ArrayList<>(List.of(
                new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
                new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
        ));

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
        }

        @Override
        public Optional<Playlist> findById(String id) {
            if ("1".equals(id)) {
                return Optional.of(new Playlist("1", "Rock Classics"));
            }
            return Optional.empty();
        }

        @Override
        public Optional<Playlist> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public List<Playlist> findAll() {
            return Collections.emptyList();
        }

        @Override
        public boolean existsByName(String name) {
            return false;
        }

        @Override
        public void addTrackToPlaylist(String playlistId, String trackId) {
            if ("1".equals(playlistId) && "t1".equals(trackId)) {
                throw new IllegalArgumentException("Errore DB: Traccia già presente nella playlist.");
            }
            this.isAddTrackCalled = true;
        }

        @Override
        public void removeTrackFromPlaylist(String playlistId, String trackId) {
            this.isRemoveTrackCalled = true;
            this.tracksInPlaylist1.removeIf(t -> t.getId().equals(trackId));
        }

        @Override
        public List<Track> findTracksByPlaylistId(String playlistId) {
            if (!"1".equals(playlistId)) {
                return Collections.emptyList();
            }
            return this.tracksInPlaylist1;
        }

        @Override
        public Optional<Playlist> deleteById(String playlistId) {
            return Optional.empty();
        }
    }
    
    
    private PlaybackService playbackService;
    private Track sampleTrack;

    @BeforeEach
    void setUp() {
        // Inizializza un servizio pulito e una traccia di esempio prima di ogni test
        playbackService = new PlaybackService(new FakePlaylistRepository());
        sampleTrack = new Track("t-100", "Stairway to Heaven", "Led Zeppelin", 482, "Rock", 1971);
    }

    // ===================================================================================
    // TEST TASK T-51 (US-09): AVVIO PLAYBACK
    // ===================================================================================

    @Test
    void testPlayTrack_TracciaEsistenteEStatoPlaying() {
        // Verifica stato iniziale
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());

        // Esecuzione
        playbackService.playTrack(sampleTrack);

        // Verifica US-09: Stato aggiornato a PLAYING e traccia impostata
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState(), "Lo stato deve passare a PLAYING");
        assertEquals(sampleTrack, playbackService.getCurrentTrack(), "La traccia corrente deve corrispondere a quella avviata");
        
        // Verifica del DTO Snapshot (opzionale ma ottima per la coverage)
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(sampleTrack, snapshot.currentTrack());
    }

    @Test
    void testPlayTrack_TracciaInesistenteNulla() {
        // Esecuzione e Verifica US-09: Gestione eccezione per traccia nulla
        assertThrows(TrackNotFoundException.class, () -> {
            playbackService.playTrack(null);
        });
        
        // Assicurati che lo stato del player non sia cambiato a causa dell'errore
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
    }

    // ===================================================================================
    // TEST TASK T-57 (US-10): PAUSA PLAYBACK
    // ===================================================================================

    @Test
    void testPause_DaStatoPlayingMantieneTraccia() {
        // Setup: Avviamo una canzone per mettere il player in stato PLAYING
        playbackService.playTrack(sampleTrack);
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());

        // Esecuzione: Premiamo pausa
        playbackService.pause();

        // Verifica US-10: Transizione a PAUSED e la traccia deve rimanere nel player
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState(), "Lo stato deve passare a PAUSED");
        assertEquals(sampleTrack, playbackService.getCurrentTrack(), "La traccia corrente deve rimanere invariata in pausa");
    }

    @Test
    void testPause_DaStatoStoppedOPausedSenzaErrori() {
        // Test 1: Pausa mentre la musica è già fermata (STOPPED)
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertDoesNotThrow(() -> playbackService.pause(), "Chiamare pause da STOPPED non deve lanciare eccezioni o crashare il sistema");
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState(), "Lo stato deve rimanere STOPPED");

        // Test 2: Pausa mentre la musica è già in pausa (PAUSED)
        playbackService.playTrack(sampleTrack);
        playbackService.pause(); // Passa a PAUSED
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        
        assertDoesNotThrow(() -> playbackService.pause(), "Chiamare pause quando è già in PAUSED non deve lanciare eccezioni");
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState(), "Lo stato deve rimanere PAUSED");
    }
}