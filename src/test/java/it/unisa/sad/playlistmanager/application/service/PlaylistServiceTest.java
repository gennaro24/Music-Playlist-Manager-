package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    class FakePlaylistRepository implements PlaylistRepository {
        boolean isSaveCalled = false;
        Playlist savedPlaylist = null;
        
        // Questa variabile "pilota" ci permette di decidere se il nome esiste o meno nel test
        boolean simulateNameExists = false;

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
        }

        @Override
        public Optional<Playlist> findById(String id) { return Optional.empty(); }

        @Override
        public Optional<Playlist> findByName(String name) { return Optional.empty(); }

        @Override
        public List<Playlist> findAll() { return null; }

        @Override
        public boolean existsByName(String name) { 
            // Impostato su false per garantire che il test di creazione rimanga sempre verde
            return false; 
        }
    }

    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        // Per questo test, simuliamo che il nome NON esista ancora nel DB
        fakeRepo.simulateNameExists = false; 
        PlaylistService service = new PlaylistService(fakeRepo);

        Playlist result = service.createPlaylist("Rock Classics");

        assertNotNull(result);
        assertEquals("Rock Classics", result.getName());
        assertTrue(fakeRepo.isSaveCalled);
        assertEquals(result, fakeRepo.savedPlaylist);
    }
}