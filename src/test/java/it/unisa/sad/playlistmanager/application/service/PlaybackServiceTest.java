package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.FakePlaylistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackServiceTest {

    private PlaybackService playbackService;
    private Track sampleTrack;

    @BeforeEach
    void setUp() {
        playbackService = new PlaybackService(new FakePlaylistRepository());
        sampleTrack = new Track("t-100", "Stairway to Heaven", "Led Zeppelin", 482, "Rock", 1971);
    }

    @Test
    void testPlayTrack_TracciaEsistenteEStatoPlaying() {
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());

        playbackService.playTrack(sampleTrack);

        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(sampleTrack, playbackService.getCurrentTrack());
        
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(sampleTrack, snapshot.currentTrack());
    }

    @Test
    void testPlayTrack_TracciaInesistenteNulla() {
        assertThrows(TrackNotFoundException.class, () -> playbackService.playTrack(null));
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
    }

    @Test
    void testPause_DaStatoPlayingMantieneTraccia() {
        playbackService.playTrack(sampleTrack);
        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack, playbackService.getCurrentTrack());
    }

    @Test
    void testPause_DaStatoStoppedOPausedSenzaErrori() {
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertDoesNotThrow(() -> playbackService.pause());
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());

        playbackService.playTrack(sampleTrack);
        playbackService.pause();
        assertDoesNotThrow(() -> playbackService.pause());
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
    }

    @Test
    void testPause_MantieneTracciaCorrenteEStatoPaused() {
        playbackService.playTrack(sampleTrack);
        playbackService.tick();
        int elapsedBeforePause = playbackService.getElapsedSeconds();

        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack, playbackService.getCurrentTrack());
        assertEquals(elapsedBeforePause, playbackService.getElapsedSeconds());
    }

    @Test
    void testTick_NonAvanzaQuandoPlayerEPaused() {
        playbackService.playTrack(sampleTrack);
        playbackService.tick();
        playbackService.pause();
        
        int elapsedAtPause = playbackService.getElapsedSeconds();
        playbackService.tick();
        playbackService.tick();

        assertEquals(elapsedAtPause, playbackService.getElapsedSeconds());
    }

    @Test
    void testPlayPlaylist_PopolataParteDallaPrimaTraccia() {
        playbackService.playPlaylist("1");

        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertNotNull(playbackService.getCurrentPlaylist());
        assertEquals("1", playbackService.getCurrentPlaylist().getId());
        assertNotNull(playbackService.getCurrentTrack());
        assertEquals("t1", playbackService.getCurrentTrack().getId());
        assertEquals(0, playbackService.getCurrentQueueIndex());
    }

    @Test
    void testPlayPlaylist_VuotaNonAvviaIlPlayback() {
        assertThrows(ValidationException.class, () -> playbackService.playPlaylist("2"));
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentPlaylist());
    }

    @Test
    void testPause_PausaPlaylistMantieneIndiceETraccia() {
        playbackService.playPlaylist("1");
        playbackService.tick();
        playbackService.tick();
        int elapsedBeforePause = playbackService.getElapsedSeconds();

        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals("1", playbackService.getCurrentPlaylist().getId());
        assertEquals("t1", playbackService.getCurrentTrack().getId());
        assertEquals(0, playbackService.getCurrentQueueIndex());
        assertEquals(elapsedBeforePause, playbackService.getElapsedSeconds());
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