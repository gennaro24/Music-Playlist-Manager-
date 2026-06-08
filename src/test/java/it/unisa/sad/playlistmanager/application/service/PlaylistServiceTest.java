package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    // ===================================================================================
    // FAKE REPOSITORIES PER SIMULARE IL DATABASE
    // ===================================================================================

    class FakePlaylistRepository implements PlaylistRepository {
        boolean isSaveCalled = false;
        boolean isAddTrackCalled = false;
        boolean isRemoveTrackCalled = false;
        Playlist savedPlaylist = null;
        
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
        public Optional<Playlist> findByName(String name) { return Optional.empty(); }

        @Override
        public List<Playlist> findAll() { return Collections.emptyList(); }

        @Override
        public boolean existsByName(String name) { return false; }

        @Override
        public void addTrackToPlaylist(String playlistId, String trackId) {
            // Simuliamo il database che blocca l'inserimento di un duplicato
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
            // La playlist "1" contiene già la traccia "t1" e "t2"
            return List.of(
                    new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
                    new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
            );
        }
    }

    class FakeTrackRepository implements TrackRepository {
        @Override
        public Optional<Track> findById(String id) {
            if ("t1".equals(id) || "t3-nuova".equals(id)) {
                return Optional.of(new Track(id, "Titolo", "Autore", 200, "Pop", 2022));
            }
            return Optional.empty();
        }
        //TODO: Implementa casi di test per il metodo update
        @Override public void save(Track track) {}
        //TODO: Implementa casi di test per il metodo update
        @Override public List<Track> findAll() { return Collections.emptyList(); }
        //TODO: Implementa casi di test per il metodo update
        @Override public Optional<Track> update(Track track){return Optional.empty();}
        //TODO: Implementa casi di test per il metodo deleteById
        @Override public Optional<Track> deleteById(String id){return Optional.empty();}
    }

    // ===================================================================================
    // TEST PREESISTENTI (US-05 e US-06) - NON TOCCARE
    // ===================================================================================

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

    // ===================================================================================
    // TEST TASK T-36 (US-07): AGGIUNTA VALIDA, PLAYLIST INESISTENTE, DUPLICATO
    // ===================================================================================

    @Test
    void testAddTrackToPlaylist_AggiuntaValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Aggiungiamo una traccia nuova ("t3-nuova") alla playlist esistente ("1")
        assertDoesNotThrow(() -> {
            service.addTrackToPlaylist("1", "t3-nuova");
        });

        // Verifichiamo che il service abbia delegato al DB l'inserimento
        assertTrue(fakePlaylistRepo.isAddTrackCalled, "Il metodo addTrackToPlaylist del repository deve essere invocato.");
    }

    @Test
    void testAddTrackToPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Passiamo una playlist che non esiste ("999")
        Exception exception = assertThrows(PlaylistNotFoundException.class, () -> {
            service.addTrackToPlaylist("999", "t1");
        });
    }

    @Test
    void testAddTrackToPlaylist_Duplicato() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Proviamo a reinserire "t1" nella playlist "1" (che ce l'ha già)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.addTrackToPlaylist("1", "t1");
        });
        assertNotNull(exception.getMessage());
    }

    // ===================================================================================
    // TEST TASK T-43 (US-08): RIMOZIONE TRACCIA
    // ===================================================================================

    @Test
    void testRemoveTrackFromPlaylist_RimozioneValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> {
            service.removeTrackFromPlaylist("1", "t1"); 
        });
        assertTrue(fakePlaylistRepo.isRemoveTrackCalled, "Il metodo removeTrackFromPlaylist del repository deve essere delegato correttamente.");
    }

    @Test
    void testRemoveTrackFromPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("999", "t1");
        });
    }

    @Test
    void testRemoveTrackFromPlaylist_TracciaInesistenteNelCatalogo() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Proviamo a rimuovere una traccia che NON esiste nel database delle tracce ("t999")
        assertThrows(TrackNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("1", "t999");
        });
    }
}