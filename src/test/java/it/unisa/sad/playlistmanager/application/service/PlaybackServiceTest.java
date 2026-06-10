package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackServiceTest {

    private PlaybackService playbackService;
    private Track sampleTrack1;
    private Track sampleTrack2;
    private Playlist samplePlaylist;
    private List<Track> playlistTracks;

    @BeforeEach
    void setUp() {
        playbackService = new PlaybackService();
        
        // Inizializzazione tracce di test
        sampleTrack1 = new Track("t-100", "Stairway to Heaven", "Led Zeppelin", 482, "Rock", 1971);
        sampleTrack2 = new Track("t-101", "Black Dog", "Led Zeppelin", 294, "Rock", 1971);
        
        // Inizializzazione playlist di test con 2 brani
        samplePlaylist = new Playlist("p-1", "My Rock Playlist");
        playlistTracks = new ArrayList<>();
        playlistTracks.add(sampleTrack1);
        playlistTracks.add(sampleTrack2);
    }

    // ===================================================================================
    // TEST PRE-ESISTENTI: AVVIO E PAUSA (US-09 / US-10)
    // ===================================================================================

    @Test
    void testPlayTrack_TracciaEsistenteEStatoPlaying() {
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());

        playbackService.playTrack(sampleTrack1);
        playbackService.playTrack(sampleTrack1);

        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
    }

    @Test
    void testPlayTrack_TracciaInesistenteNulla() {
        assertThrows(TrackNotFoundException.class, () -> {
            playbackService.playTrack(null);
        });
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
    }

    @Test
    void testPause_DaStatoPlayingMantieneTraccia() {
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertDoesNotThrow(() -> playbackService.pause());
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());

        playbackService.playTrack(sampleTrack1);
        playbackService.pause();
        assertDoesNotThrow(() -> playbackService.pause());
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
    }

    @Test
    void testPause_MantieneTracciaCorrenteEStatoPaused() {
        playbackService.playTrack(sampleTrack1);
        playbackService.tick();
        int elapsedBeforePause = playbackService.getElapsedSeconds();

        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(elapsedBeforePause, playbackService.getElapsedSeconds());
    }

    @Test
    void testTick_NonAvanzaQuandoPlayerEPaused() {
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.tick();
        playbackService.pause();
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN: Premo Skip mentre il player è in pausa
        playbackService.skipToNext();

        // THEN: La traccia corrente cambia, ma lo stato resta rigorosamente PAUSED
        assertEquals(sampleTrack2, playbackService.getCurrentTrack(), "La traccia deve cambiare anche se in pausa");
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState(), "Il player deve preservare lo stato PAUSED");
    }

    // ===================================================================================
    // TEST TASK (US-17): VERIFICA COERENZA DEGLI SNAPSHOT DTO
    // ===================================================================================

    @Test
    void testPlayPlaylist_PopolataParteDallaPrimaTraccia() {
        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertNotNull(playbackService.getCurrentPlaylist());
        assertEquals(samplePlaylist.getId(), playbackService.getCurrentPlaylist().getId());
        assertNotNull(playbackService.getCurrentTrack());
        assertEquals(sampleTrack1.getId(), playbackService.getCurrentTrack().getId());
        assertEquals(0, playbackService.getCurrentQueueIndex());
    }

    @Test
    void testPlayPlaylist_VuotaNonAvviaIlPlayback() {
        assertThrows(IllegalArgumentException.class, () -> playbackService.playPlaylist(samplePlaylist, new ArrayList<>()));
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentPlaylist());
    }

    @Test
    void testPause_PausaPlaylistMantieneIndiceETraccia() {
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.tick();
        playbackService.tick();
        int elapsedBeforePause = playbackService.getElapsedSeconds();

        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(samplePlaylist.getId(), playbackService.getCurrentPlaylist().getId());
        assertEquals(sampleTrack1.getId(), playbackService.getCurrentTrack().getId());
        assertEquals(0, playbackService.getCurrentQueueIndex());
        assertEquals(elapsedBeforePause, playbackService.getElapsedSeconds());
    }

    // ===================================================================================
    // TEST LOOP E SHUFFLE (US-11 / US-15)
    // ===================================================================================

    @Test
    void testFineTraccia_RepeatOneRiparteStessaTraccia() {
        playbackService.playTrack(sampleTrack1);
        playbackService.enableSingleTrackLoopMode();

        for (int i = 0; i < sampleTrack1.getDuration(); i++) {
            playbackService.tick();
        }

        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(sampleTrack1, snapshot.currentTrack());
        assertEquals(0, playbackService.getElapsedSeconds());
        assertEquals(PlaybackMode.REPEAT_ONE, playbackService.getCurrentMode());
    }

    @Test
    void testCambioLoopSingoloASequential_NonResettaTracciaCorrente() {
        playbackService.playTrack(sampleTrack1);
        playbackService.enableSingleTrackLoopMode();

        playbackService.disableSingleTrackLoopMode();

        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.SEQUENTIAL, playbackService.getCurrentMode());
        assertEquals(sampleTrack1, snapshot.currentTrack());
        assertEquals(PlaybackState.PLAYING, snapshot.state());
    }

    @Test
    void testSkipToNext_InRepeatAllDaUltimaTracciaRiparteDallaPrima() {
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext();
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        playbackService.skipToNext();

        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    @Test
    void testFineTraccia_InRepeatAllRiparteDallaPrimaTraccia() {
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext();
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        for (int i = 0; i < sampleTrack2.getDuration(); i++) {
            playbackService.tick();
        }

        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(sampleTrack1, snapshot.currentTrack());
        assertEquals(0, playbackService.getElapsedSeconds());
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode());
    }

    /**
     * Verifica il requisito T-143: Il passaggio alla modalità SHUFFLE a caldo
     * non deve interrompere lo stato PLAYING, né cambiare la traccia corrente,
     * né azzerare i secondi già trascorsi.
     */
    @Test
    void testSetPlaybackModeShuffleNonInterrompeTracciaCorrente() {
        // Setup base
        PlaybackService service = new PlaybackService(new it.unisa.sad.playlistmanager.persistence.repository.FakePlaylistRepository());
        Track track = new Track("t1", "Test Song", "Artist", 200, "Pop", 2020);
        
        // Avviamo la riproduzione
        service.playTrack(track);
        
        // Facciamo avanzare la canzone di 3 secondi (3 tick)
        service.tick();
        service.tick();
        service.tick();
        
        // Verifichiamo lo stato prima dello Shuffle
        assertEquals(PlaybackState.PLAYING, service.getCurrentState());
        assertEquals(3, service.getElapsedSeconds());
        
        // ESECUZIONE: L'utente preme il tasto Shuffle
        service.setPlaybackMode(it.unisa.sad.playlistmanager.domain.model.PlaybackMode.SHUFFLE);
        
        // VERIFICA: Il player NON deve essersi fermato o resettato!
        assertEquals(it.unisa.sad.playlistmanager.domain.model.PlaybackMode.SHUFFLE, service.getCurrentMode(), "La modalità deve essere SHUFFLE");
        assertEquals(PlaybackState.PLAYING, service.getCurrentState(), "La riproduzione NON deve essersi interrotta");
        assertEquals(track, service.getCurrentTrack(), "La traccia corrente deve rimanere la stessa");
        assertEquals(3, service.getElapsedSeconds(), "I secondi trascorsi NON devono azzerarsi");
    }
}