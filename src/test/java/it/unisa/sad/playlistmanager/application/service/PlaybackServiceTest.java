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

        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
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
        playbackService.playTrack(sampleTrack1);
        playbackService.pause();

        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
    }

    // ===================================================================================
    // TEST TASK (US-14): SCENARI DI SKIP TO NEXT
    // ===================================================================================

    @Test
    void testSkipToNext_Scenario1_SkipNormaleInPlaying() {
        // GIVEN: Una playlist è in riproduzione (stato PLAYING) ed esiste una traccia successiva
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack(), "Inizialmente deve riprodurre la prima traccia");

        // WHEN: Viene invocato lo skip
        playbackService.skipToNext();

        // THEN: La traccia corrente diventa la successiva e lo stato resta PLAYING
        assertEquals(sampleTrack2, playbackService.getCurrentTrack(), "La traccia corrente deve avanzare a Black Dog");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState(), "Lo stato deve rimanere PLAYING");
    }

    @Test
    void testSkipToNext_Scenario2_SkipDallUltimaTracciaSequential() {
        // GIVEN: Il player sta riproducendo l'ultima traccia della playlist in modalità SEQUENTIAL
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext(); // Avanza alla seconda (e ultima) traccia
        playbackService.setCurrentMode(PlaybackMode.SEQUENTIAL);
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        // WHEN: Premo Skip dall'ultima canzone
        playbackService.skipToNext();

        // THEN: Lo stato diventa STOPPED e la traccia corrente si azzera coerentemente
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState(), "Al termine della coda lo stato deve essere STOPPED");
        assertNull(playbackService.getCurrentTrack(), "Nessuna traccia deve essere in riproduzione");
    }

    @Test
    void testSkipToNext_Scenario3_SkipInStatoPaused() {
        // GIVEN: Una playlist è caricata, ma il player è in stato PAUSED ed esiste un brano successivo
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
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
    void testSnapshot_TaskT161_DopoPlayMostraPlayingETracciaCorrente() {
        // WHEN: Avvio il playback di una traccia
        playbackService.playTrack(sampleTrack1);
        PlaybackSnapshot snapshot = playbackService.getSnapshot();

        // THEN: Lo snapshot letto dalla UI certifica lo stato PLAYING e la traccia corretta
        assertNotNull(snapshot);
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "Lo snapshot deve riportare PLAYING");
        assertEquals(sampleTrack1, snapshot.currentTrack(), "Lo snapshot deve contenere la traccia avviata");
    }

    @Test
    void testSnapshot_TaskT162_DopoPauseMostraPausedEMantieneTraccia() {
        // WHEN: Il playback viene avviato e poi messo in pausa
        playbackService.playTrack(sampleTrack1);
        playbackService.pause();
        PlaybackSnapshot snapshot = playbackService.getSnapshot();

        // THEN: Lo snapshot mostra PAUSED e la traccia resta visibile
        assertNotNull(snapshot);
        assertEquals(PlaybackState.PAUSED, snapshot.state(), "Lo snapshot deve riportare PAUSED");
        assertEquals(sampleTrack1, snapshot.currentTrack(), "La traccia deve restare valorizzata nel DTO");
    }

    @Test
    void testSnapshot_TaskT163_DopoTermineCodaMostraStoppedCoerente() {
        // WHEN: Riproduco l'ultima traccia ed eseguo lo skip facendo terminare la coda
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext(); // Va alla seconda traccia
        playbackService.skipToNext(); // Oltre la fine della coda -> STOPPED
        
        PlaybackSnapshot snapshot = playbackService.getSnapshot();

        // THEN: Lo snapshot mostra STOPPED e nessuna traccia attiva (coerente con la decisione di design di ripulire la UI)
        assertNotNull(snapshot);
        assertEquals(PlaybackState.STOPPED, snapshot.state(), "Lo snapshot deve riportare STOPPED");
        assertNull(snapshot.currentTrack(), "Lo snapshot non deve mostrare alcuna traccia in esecuzione alla fine della coda");
    }
}