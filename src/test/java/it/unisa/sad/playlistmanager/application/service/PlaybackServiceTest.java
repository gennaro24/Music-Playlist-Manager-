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
    void testPlayPlaylist_PreservaRepeatAllSeSelezionatoPrimaDelPlay() {
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    @Test
    void testPlayPlaylist_PreservaShuffleSeSelezionatoPrimaDelPlay() {
        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);

        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        assertEquals(PlaybackMode.SHUFFLE, playbackService.getCurrentMode());
        assertNotNull(playbackService.getCurrentTrack());
        assertTrue(playlistTracks.contains(playbackService.getCurrentTrack()));
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    @Test
    void testPlayCatalog_PreservaRepeatAllSeSelezionatoPrimaDelPlay() {
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        playbackService.playCatalog(playlistTracks);

        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    @Test
    void testPlayTrack_PreservaRepeatOneSeSelezionatoPrimaDelPlay() {
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);

        playbackService.playTrack(sampleTrack1);

        assertEquals(PlaybackMode.REPEAT_ONE, playbackService.getCurrentMode());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    @Test
    void testPlayTrack_DisattivaShufflePreselezionato() {
        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);

        playbackService.playTrack(sampleTrack1);

        assertEquals(PlaybackMode.SEQUENTIAL, playbackService.getCurrentMode());
        assertFalse(playbackService.isShuffleAvailable());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
    }

    @Test
    void testSetShuffle_DuranteRiproduzioneSingolaRestaSequential() {
        playbackService.playTrack(sampleTrack1);

        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);

        assertEquals(PlaybackMode.SEQUENTIAL, playbackService.getCurrentMode());
        assertFalse(playbackService.isShuffleAvailable());
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

    // ===================================================================================
    // TEST FINE NATURALE IN REPEAT_ALL (loop playlist)
    // ===================================================================================
    //T-154
    @Test
    void testFineNaturale_RepeatAll_DaPrimaTraccia_AvanzaAllaSeconda() {
        // GIVEN: playlist avviata in REPEAT_ALL dalla prima traccia
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN: la prima traccia termina naturalmente
        for (int i = 0; i < sampleTrack1.getDuration(); i++) {
            playbackService.tick();
        }

        // THEN: avanza alla seconda traccia, rimane PLAYING, timer a 0
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(sampleTrack2, snapshot.currentTrack(), "Deve passare alla seconda traccia");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "Deve restare PLAYING");
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi");
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode(), "La modalità deve restare REPEAT_ALL");
    }

    @Test
    void testFineNaturale_RepeatAll_DaUltimaTraccia_RiparteAllaPrima() {
        // GIVEN: playlist avviata, skip manuale all'ultima traccia, REPEAT_ALL attivo
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext();
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        // WHEN: l'ultima traccia termina naturalmente
        for (int i = 0; i < sampleTrack2.getDuration(); i++) {
            playbackService.tick();
        }

        // THEN: riparte dalla prima traccia, rimane PLAYING, timer a 0
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(sampleTrack1, snapshot.currentTrack(), "Deve tornare alla prima traccia");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "Deve restare PLAYING");
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi");
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode(), "La modalità deve restare REPEAT_ALL");
    }

    @Test
    void testFineNaturale_RepeatAll_CicloCompleto_TornaAllaPrimaTraccia() {
        // GIVEN: playlist a 3 tracce in REPEAT_ALL
        Track track3 = new Track("t-102", "Whole Lotta Love", "Led Zeppelin", 334, "Rock", 1969);
        List<Track> threeTracks = new java.util.ArrayList<>(playlistTracks);
        threeTracks.add(track3);
        Playlist playlist3 = new Playlist("p-2", "Rock Extended");

        playbackService.playPlaylist(playlist3, threeTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        // WHEN: faccio finire le 3 tracce una dopo l'altra (skip manuale)
        playbackService.skipToNext(); // -> traccia 2
        playbackService.skipToNext(); // -> traccia 3
        assertEquals(track3, playbackService.getCurrentTrack());

        // e aspetto la fine naturale della terza
        for (int i = 0; i < track3.getDuration(); i++) {
            playbackService.tick();
        }

        // THEN: torna alla prima traccia
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(sampleTrack1, snapshot.currentTrack(), "Deve tornare alla prima traccia dopo il ciclo completo");
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

    @Test
    void testPlayPlaylist_ConRepeatAllGiaAttivo_PreservaModalita() {
        // GIVEN: modalità REPEAT_ALL già impostata prima di avviare la playlist
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        // WHEN: avvio la playlist
        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        // THEN: la modalità non deve essere resettata a SEQUENTIAL
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode(),
                "La modalità REPEAT_ALL deve essere preservata all'avvio della playlist");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());
    }

    @Test
    void testPlayPlaylist_ConRepeatOneGiaAttivo_PreservaModalita() {
        // GIVEN: modalità REPEAT_ONE già impostata prima di avviare la playlist
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);

        // WHEN: avvio la playlist
        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        // THEN: la modalità non deve essere resettata a SEQUENTIAL
        assertEquals(PlaybackMode.REPEAT_ONE, playbackService.getCurrentMode(),
                "La modalità REPEAT_ONE deve essere preservata all'avvio della playlist");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
    }

    /**
     * Verifica il requisito T-143: Il passaggio alla modalità SHUFFLE a caldo
     * non deve interrompere lo stato PLAYING, né cambiare la traccia corrente,
     * né azzerare i secondi già trascorsi.
     */
    @Test
    void testSetPlaybackModeShuffleNonInterrompePlaylistCorrente() {
        // Setup base
        PlaybackService service = new PlaybackService(new it.unisa.sad.playlistmanager.persistence.repository.FakePlaylistRepository());
        Track track = new Track("t1", "Test Song", "Artist", 200, "Pop", 2020);
        Track secondTrack = new Track("t2", "Second Song", "Artist", 180, "Pop", 2021);
        Playlist playlist = new Playlist("p1", "Test Playlist");

        // Avviamo la riproduzione di una coda che supporta lo shuffle
        service.playPlaylist(playlist, List.of(track, secondTrack));
        
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

    
    // ===================================================================================
    // TEST CAMBIO MODALITÀ: da LOOP a SEQUENTIAL non interrompe la traccia corrente
    // ===================================================================================
    //T-155
    @Test
    void testCambioRepeatOneASequential_NonInterrompeTraccia() {
        // GIVEN: traccia in riproduzione con REPEAT_ONE attivo e 5 secondi trascorsi
        playbackService.playTrack(sampleTrack1);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);
        for (int i = 0; i < 5; i++) playbackService.tick();
        assertEquals(5, playbackService.getElapsedSeconds());

        // WHEN: l'utente disattiva il loop tornando a SEQUENTIAL
        playbackService.setPlaybackMode(PlaybackMode.SEQUENTIAL);

        // THEN: traccia, stato e timer devono restare invariati
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.SEQUENTIAL, snapshot.mode(), "La modalità deve essere SEQUENTIAL");
        assertEquals(sampleTrack1, snapshot.currentTrack(), "La traccia corrente NON deve cambiare");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "La riproduzione NON deve interrompersi");
        assertEquals(5, playbackService.getElapsedSeconds(), "Il timer NON deve azzerarsi");
    }

    @Test
    void testCambioRepeatAllASequential_NonInterrompeTraccia() {
        // GIVEN: playlist in riproduzione con REPEAT_ALL attivo e alcuni tick trascorsi
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        for (int i = 0; i < 7; i++) playbackService.tick();
        assertEquals(7, playbackService.getElapsedSeconds());

        // WHEN: l'utente disattiva il loop tornando a SEQUENTIAL
        playbackService.setPlaybackMode(PlaybackMode.SEQUENTIAL);

        // THEN: traccia, stato e timer devono restare invariati
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.SEQUENTIAL, snapshot.mode(), "La modalità deve essere SEQUENTIAL");
        assertEquals(sampleTrack1, snapshot.currentTrack(), "La traccia corrente NON deve cambiare");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "La riproduzione NON deve interrompersi");
        assertEquals(7, playbackService.getElapsedSeconds(), "Il timer NON deve azzerarsi");
    }

    @Test
    void testCambioRepeatOneARepeatAll_NonInterrompeTraccia() {
        // GIVEN: traccia in riproduzione con REPEAT_ONE attivo e 3 secondi trascorsi
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);
        for (int i = 0; i < 3; i++) playbackService.tick();

        // WHEN: l'utente cambia da REPEAT_ONE a REPEAT_ALL
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);

        // THEN: la traccia non cambia e la riproduzione continua
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.REPEAT_ALL, snapshot.mode(), "La modalità deve essere REPEAT_ALL");
        assertEquals(sampleTrack1, snapshot.currentTrack(), "La traccia corrente NON deve cambiare");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "La riproduzione NON deve interrompersi");
        assertEquals(3, playbackService.getElapsedSeconds(), "Il timer NON deve azzerarsi");
    }

    @Test
    void testCambioRepeatAllARepeatOne_NonInterrompeTraccia() {
        // GIVEN: playlist con REPEAT_ALL, avanzo alla seconda traccia e aspetto 4 tick
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        playbackService.skipToNext();
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());
        for (int i = 0; i < 4; i++) playbackService.tick();

        // WHEN: l'utente cambia da REPEAT_ALL a REPEAT_ONE
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);

        // THEN: la traccia 2 continua a suonare senza reset
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.REPEAT_ONE, snapshot.mode(), "La modalità deve essere REPEAT_ONE");
        assertEquals(sampleTrack2, snapshot.currentTrack(), "Deve restare la seconda traccia");
        assertEquals(PlaybackState.PLAYING, snapshot.state(), "La riproduzione NON deve interrompersi");
        assertEquals(4, playbackService.getElapsedSeconds(), "Il timer NON deve azzerarsi");
    }

    @Test
    void testCambioRepeatOneASequential_DaStatoPaused_NonResettaTracciaETimer() {
        // GIVEN: traccia in pausa con REPEAT_ONE attivo e 10 secondi trascorsi
        playbackService.playTrack(sampleTrack1);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);
        for (int i = 0; i < 10; i++) playbackService.tick();
        playbackService.pause();
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());

        // WHEN: l'utente disattiva il loop stando in pausa
        playbackService.setPlaybackMode(PlaybackMode.SEQUENTIAL);

        // THEN: rimane in pausa, stessa traccia, stesso timer
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(PlaybackMode.SEQUENTIAL, snapshot.mode());
        assertEquals(sampleTrack1, snapshot.currentTrack(), "La traccia NON deve cambiare");
        assertEquals(PlaybackState.PAUSED, snapshot.state(), "Lo stato deve restare PAUSED");
        assertEquals(10, playbackService.getElapsedSeconds(), "Il timer NON deve azzerarsi");
    }

    // ===================================================================================
    // TEST SKIP – MODALITÀ SEQUENTIAL
    // ===================================================================================

    @Test
    void testSkip_Sequential_AvanzaAllaTracciaSuccessiva() {
        // GIVEN: playlist con 2 tracce in modalità SEQUENTIAL
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN
        playbackService.skipToNext();

        // THEN: si è avanzati alla seconda traccia, stato rimasto PLAYING
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(PlaybackMode.SEQUENTIAL, playbackService.getCurrentMode());
    }

    @Test
    void testSkip_Sequential_ResetTimer() {
        // GIVEN: playlist avviata, tick di 5 secondi
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        for (int i = 0; i < 5; i++) playbackService.tick();
        assertEquals(5, playbackService.getElapsedSeconds());

        // WHEN
        playbackService.skipToNext();

        // THEN: il timer si azzera sulla nuova traccia
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi dopo lo skip");
    }

    @Test
    void testSkip_Sequential_DaUltimaTraccia_SiFerma() {
        // GIVEN: playlist con 2 tracce, avanziamo già alla seconda (ultima)
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext();
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        // WHEN: skip sull'ultima traccia
        playbackService.skipToNext();

        // THEN: player fermo, nessuna traccia corrente, timer azzerato
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

    @Test
    void testSkip_SenzaCoda_NoOp() {
        // GIVEN: nessuna riproduzione attiva
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());

        // WHEN: skip invocato senza coda
        assertDoesNotThrow(() -> playbackService.skipToNext());

        // THEN: lo stato non cambia
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());
    }

    @Test
    void testSkip_DaStatoPaused_TracciaAvanzaStatoRestaPaused() {
        // GIVEN: playlist avviata, player messo in pausa
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.pause();
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState());
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN: skip mentre in pausa
        playbackService.skipToNext();

        // THEN: la traccia avanza ma lo stato resta PAUSED (comportamento a livello service;
        //       è il controller che decide se riprendere la riproduzione)
        assertEquals(sampleTrack2, playbackService.getCurrentTrack(), "La traccia deve cambiare");
        assertEquals(PlaybackState.PAUSED, playbackService.getCurrentState(), "Lo stato deve restare PAUSED");
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi");
    }

    // ===================================================================================
    // TEST SKIP – MODALITÀ SHUFFLE
    // ===================================================================================

    @Test
    void testSkip_Shuffle_TracciaSuccessivaAppartieneAllaPlaylist() {
        // GIVEN: modalità SHUFFLE attivata prima della playlist
        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        Track primaTraccia = playbackService.getCurrentTrack();
        assertNotNull(primaTraccia);

        // WHEN
        playbackService.skipToNext();

        // THEN: la traccia cambia ed è comunque nella playlist
        Track dopoSkip = playbackService.getCurrentTrack();
        assertNotNull(dopoSkip, "Ci deve essere una traccia dopo lo skip");
        assertNotEquals(primaTraccia, dopoSkip, "La traccia deve essere diversa da quella precedente");
        assertTrue(playlistTracks.contains(dopoSkip), "La nuova traccia deve far parte della playlist");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi");
    }

    @Test
    void testSkip_Shuffle_EsauritaCoda_SiFerma() {
        // GIVEN: playlist con 2 tracce in SHUFFLE
        // startQueue consuma index=0 della coda mescolata → ne rimangono 1
        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);

        // WHEN: skip esaurisce la coda mescolata
        playbackService.skipToNext(); // consuma index=1 → ok
        playbackService.skipToNext(); // index=2 >= size=2 → null → STOPPED

        // THEN
        assertEquals(PlaybackState.STOPPED, playbackService.getCurrentState());
        assertNull(playbackService.getCurrentTrack());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

    @Test
    void testSkip_Shuffle_ResetTimer() {
        // GIVEN: SHUFFLE attivo, 7 tick trascorsi
        playbackService.setPlaybackMode(PlaybackMode.SHUFFLE);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        for (int i = 0; i < 7; i++) playbackService.tick();
        assertEquals(7, playbackService.getElapsedSeconds());

        // WHEN
        playbackService.skipToNext();

        // THEN
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi dopo lo skip in SHUFFLE");
    }

    // ===================================================================================
    // TEST SKIP – MODALITÀ REPEAT_ALL
    // ===================================================================================

    @Test
    void testSkip_RepeatAll_AvanzaAllaTracciaSuccessiva() {
        // GIVEN: playlist con REPEAT_ALL, sulla prima traccia
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN
        playbackService.skipToNext();

        // THEN
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

    @Test
    void testSkip_RepeatAll_DaUltimaTraccia_TornaAllaPrima() {
        // GIVEN: playlist con REPEAT_ALL, già sull'ultima traccia
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.skipToNext(); // → sampleTrack2 (ultima)
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        // WHEN
        playbackService.skipToNext();

        // THEN: torna alla prima traccia, rimane PLAYING
        assertEquals(sampleTrack1, playbackService.getCurrentTrack(), "Deve tornare alla prima traccia");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(PlaybackMode.REPEAT_ALL, playbackService.getCurrentMode());
    }

    @Test
    void testSkip_RepeatAll_ResetTimer() {
        // GIVEN
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ALL);
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        for (int i = 0; i < 10; i++) playbackService.tick();

        // WHEN
        playbackService.skipToNext();

        // THEN
        assertEquals(0, playbackService.getElapsedSeconds(), "Il timer deve azzerarsi dopo lo skip in REPEAT_ALL");
    }

    // ===================================================================================
    // TEST SKIP – MODALITÀ REPEAT_ONE
    // ===================================================================================

    @Test
    void testSkip_RepeatOne_AvanzaAllaTracciaSuccessiva() {
        // GIVEN: playlist con REPEAT_ONE attivo sulla prima traccia
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);
        assertEquals(sampleTrack1, playbackService.getCurrentTrack());

        // WHEN: lo skip manuale deve forzare l'avanzamento ignorando il loop
        playbackService.skipToNext();

        // THEN: si è passati alla seconda traccia; la modalità resta REPEAT_ONE
        assertEquals(sampleTrack2, playbackService.getCurrentTrack(), "Lo skip deve superare il loop e andare al brano successivo");
        assertEquals(PlaybackMode.REPEAT_ONE, playbackService.getCurrentMode(), "La modalità non deve cambiare");
        assertEquals(PlaybackState.PLAYING, playbackService.getCurrentState());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

    @Test
    void testFineNaturale_RepeatOne_DopoSkip_RiparteNuovaTraccia() {
        // GIVEN: passiamo con lo skip alla seconda traccia in REPEAT_ONE
        playbackService.playPlaylist(samplePlaylist, playlistTracks);
        playbackService.setPlaybackMode(PlaybackMode.REPEAT_ONE);
        playbackService.skipToNext(); // → sampleTrack2
        assertEquals(sampleTrack2, playbackService.getCurrentTrack());

        // WHEN: la seconda traccia termina naturalmente
        for (int i = 0; i < sampleTrack2.getDuration(); i++) {
            playbackService.tick();
        }

        // THEN: il loop si attiva sulla nuova traccia (sampleTrack2) e riparte da 0
        PlaybackSnapshot snapshot = playbackService.getSnapshot();
        assertEquals(sampleTrack2, snapshot.currentTrack(), "Il loop deve ripetersi sulla nuova traccia");
        assertEquals(PlaybackState.PLAYING, snapshot.state());
        assertEquals(PlaybackMode.REPEAT_ONE, snapshot.mode());
        assertEquals(0, playbackService.getElapsedSeconds());
    }

}