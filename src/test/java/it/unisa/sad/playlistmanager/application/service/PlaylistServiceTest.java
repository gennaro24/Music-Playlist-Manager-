package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

class PlaylistServiceTest {

    /**
     * 1. CREAZIONE DEL FAKE REPOSITORY
     * Simuliamo il database delle playlist.
     */
    class FakePlaylistRepository implements PlaylistRepository {
        boolean isSaveCalled = false;
        Playlist savedPlaylist = null;

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
        }

        // da implementare: >>>>
        public Optional<Playlist> findById(String id){return Optional.empty();}


        public Optional<Playlist> findByName(String name){return Optional.empty();}


        public List<Playlist> findAll(){return null;}

 
        public boolean existsByName(String name){return true;}

        //end <<<< 

        
    }

    /**
     * 2. IL TEST VERO E PROPRIO
     */
    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        // PREPARAZIONE
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        PlaylistService service = new PlaylistService(fakeRepo);

        // ESECUZIONE
        Playlist result = service.createPlaylist("Rock Classics");

        // VERIFICA
        assertNotNull(result, "Errore: La playlist restituita è null!");
        
        // Supponendo che la classe Playlist abbia un metodo getName()
        assertEquals("Rock Classics", result.getName(), "Errore: Il nome della playlist non corrisponde!");

        // Controlliamo che il repository finto sia stato chiamato
        assertTrue(fakeRepo.isSaveCalled, "Errore: Il PlaylistService non ha chiamato save() sul repository!");
        assertEquals(result, fakeRepo.savedPlaylist, "Errore: La playlist salvata non è quella creata!");
    }
}