package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackServiceTest {

    private PlaybackService playbackService;
    private Track sampleTrack;

    @BeforeEach
    void setUp() {
        // Inizializza un servizio pulito e una traccia di esempio prima di ogni test
        playbackService = new PlaybackService();
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

    // ===================================================================================
    // TEST TASK T-111 & T-112 (US-10): PAUSA MANTIENE STATO E TICK NON AVANZA
    // ===================================================================================

    @Test
    void testPause_MantieneTracciaCorrenteEStatoPaused() {
        // T-111: Pausa mantiene traccia corrente e stato PAUSED
        // Setup: Avvia il playback
        playbackService.playTrack(sampleTrack);
        
        // Esegui un tick per simulare un secondo di riproduzione
        playbackService.tick();
        int elapsedBeforePause = playbackService.getElapsedSeconds();
        assertEquals(1, elapsedBeforePause, "Il tempo dovrebbe essere 1 secondo dopo un tick");

        // Esecuzione: Premiamo pausa
        playbackService.pause();

        // Verifica T-111
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState(), "Lo stato deve essere PAUSED");
        assertEquals(sampleTrack, playbackService.getCurrentTrack(), "La traccia in pausa deve essere quella avviata");
        assertEquals(elapsedBeforePause, playbackService.getElapsedSeconds(), "I secondi trascorsi devono essere preservati");
    }

    @Test
    void testTick_NonAvanzaQuandoPlayerEPaused() {
        // T-112: tick() non avanza quando player è PAUSED
        // Setup: Avvia e poi metti in pausa
        playbackService.playTrack(sampleTrack);
        playbackService.tick(); // Avanza a 1 secondo
        playbackService.pause();
        
        // Assicuriamoci che sia effettivamente in pausa a 1 secondo
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        int elapsedAtPause = playbackService.getElapsedSeconds();
        assertEquals(1, elapsedAtPause);

        // Esecuzione: Simuliamo il passare del tempo (tick) mentre è in pausa
        playbackService.tick();
        playbackService.tick();

        // Verifica T-112: Il tempo non deve essere avanzato
        assertEquals(elapsedAtPause, playbackService.getElapsedSeconds(), "Il tempo trascorso non deve aumentare se lo stato è PAUSED");
    }
}