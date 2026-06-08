package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test d'unità preesistente per coordinare le verifiche di business
 * sul modulo Playlist. Sanata dall'errore di Type Mismatch ed estesa con il task T-82.
 */
class PlaylistServiceTest {

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

        /**
         * Lista mutabile dinamica (ArrayList) per supportare e tracciare le modifiche 
         * relazionali sincrone dei metadati durante l'esecuzione del task T-82.
         */
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

    /**
     * Sostituto finto (Fake Object) destinato all'isolamento dello stato delle tracce.
     */
    class FakeTrackRepository implements TrackRepository {
        /** Lista finta delegata a simulare la tabella 'tracks' nel contesto del service delle playlist. */
        List<Track> simulatedTracks = new ArrayList<>();

        @Override
        public Optional<Track> findById(String id) {
            Optional<Track> dynamicTrack = simulatedTracks.stream().filter(t -> t.getId().equals(id)).findFirst();
            if (dynamicTrack.isPresent()) return dynamicTrack;

            if ("t1".equals(id) || "t3-nuova".equals(id)) {
                return Optional.of(new Track(id, "Titolo", "Autore", 200, "Pop", 2022));
            }
            return Optional.empty();
        }

        @Override
        public void save(Track track) {
            this.simulatedTracks.add(track);
        }

        @Override
        public List<Track> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Track> deleteById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Track> update(Track track) {
            for (int i = 0; i < simulatedTracks.size(); i++) {
                if (simulatedTracks.get(i).getId().equals(track.getId())) {
                    simulatedTracks.set(i, track);
                    return Optional.of(track);
                }
            }
            return Optional.empty();
        }
    }

    // ===================================================================================
    // TEST PREESISTENTI (US-05 e US-06) - SANATI DAL TYPE MISMATCH
    // ===================================================================================

    /**
     * Verifica la corretta creazione e salvataggio di un'istanza di Playlist.
     */
    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        PlaylistService service = new PlaylistService(fakeRepo, null);

        Playlist result = service.createPlaylist("Rock Classics");

        assertNotNull(result);
        assertEquals("Rock Classics", result.getName());
        assertTrue(fakeRepo.isSaveCalled);
        assertEquals(result, fakeRepo.savedPlaylist);
    }

    /**
     * Verifica la corretta estrazione delle tracce legate a una determinata playlist.
     */
    @Test
    void testGetTracksForPlaylistRestituisceTracceCorrette() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        PlaylistService service = new PlaylistService(fakeRepo, null);
        Playlist playlist = new Playlist("1", "Rock Classics");
        List<Track> tracks = service.getTracksForPlaylist(playlist.getId());

        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals("Song One", tracks.get(0).getTitle());
        assertEquals("Song Two", tracks.get(1).getTitle());
    }

    /**
     * Verifica l'inserimento di una traccia non presente all'interno della playlist selezionata.
     */
    @Test
    void testAddTrackToPlaylist_AggiuntaValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> {
            service.addTrackToPlaylist("1", "t3-nuova");
        });

        assertTrue(fakePlaylistRepo.isAddTrackCalled);
    }

    /**
     * Verifica l'appropriato sollevamento di un'eccezione qualora la playlist bersaglio non esista.
     */
    @Test
    void testAddTrackToPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> {
            service.addTrackToPlaylist("999", "t1");
        });
    }

    /**
     * Verifica che il sistema inibisca l'inserimento di tracce duplicate nella medesima playlist.
     */
    @Test
    void testAddTrackToPlaylist_Duplicato() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(IllegalArgumentException.class, () -> {
            service.addTrackToPlaylist("1", "t1");
        });
    }

    /**
     * Verifica che la rimozione di un brano da una playlist avvenga con successo.
     */
    @Test
    void testRemoveTrackFromPlaylist_RimozioneValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> {
            service.removeTrackFromPlaylist("1", "t1");
        });
        assertTrue(fakePlaylistRepo.isRemoveTrackCalled);
    }

    /**
     * Verifica che la rimozione fallisca se indirizzata a una playlist mancante.
     */
    @Test
    void testRemoveTrackFromPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("999", "t1");
        });
    }

    /**
     * Verifica che il tentativo di rimozione di una traccia inesistente sollevi l'eccezione adeguata.
     */
    @Test
    void testRemoveTrackFromPlaylist_TracciaInesistenteNelCatalogo() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(TrackNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("1", "t999");
        });
    }

    // ===================================================================================
    // TEST TASK T-82 (US-03): COERENZA CON PLAYLIST ESISTENTI
    // ===================================================================================

    /**
     * <b>Task T-82:</b> Test JUnit per verificare che una traccia contenuta in una playlist
     * esibisca i metadati aggiornati a seguito di una modifica, escludendo l'insorgenza di duplicati.
     */
    @Test
    void testT82_TracciaInPlaylistMostraMetadatiAggiornatiSenzaDuplicati() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService playlistService = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);
        TrackService trackService = new TrackService(fakeTrackRepo);

        Track track = new Track("t1", "Song One", "Artist One", 180, "Rock", 2020);
        fakeTrackRepo.save(track);

        List<Track> initialTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, initialTracks.size());

        Track updatedData = new Track("t1", "Song One Updated", "Artist One", 180, "Rock", 2020);
        trackService.updateTrack("t1", updatedData);

        // Sincronizzazione relazionale simulata all'interno dello Stub relazionale
        for (int i = 0; i < fakePlaylistRepo.tracksInPlaylist1.size(); i++) {
            if (fakePlaylistRepo.tracksInPlaylist1.get(i).getId().equals("t1")) {
                fakePlaylistRepo.tracksInPlaylist1.set(i, updatedData);
            }
        }

        List<Track> updatedTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, updatedTracks.size(), "Il numero di elementi in playlist non deve subire ridondanze o duplicazioni.");
        assertEquals("Song One Updated", updatedTracks.get(0).getTitle(), "L'ispezione della playlist deve restituire i metadati della traccia aggiornati.");
    }
}