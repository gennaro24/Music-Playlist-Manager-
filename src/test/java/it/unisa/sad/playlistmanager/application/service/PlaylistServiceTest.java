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
        
        // Questa variabile "pilota" ci permette di decidere se il nome esiste o meno nel test
        boolean simulateNameExists = false;

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
        }

        // Metodi dell'interfaccia implementati per evitare errori di compilazione
        @Override
        public Optional<Playlist> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Playlist> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public List<Playlist> findAll() {
            return null;
        }

        @Override
        public boolean existsByName(String name) {
            // Ritorna il valore che abbiamo impostato nel test specifico
            return this.simulateNameExists;
        }
    }

    /**
     * 2. I TEST
     */
    
    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        // PREPARAZIONE
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        // Per questo test, simuliamo che il nome NON esista ancora nel DB
        fakeRepo.simulateNameExists = false; 
        PlaylistService service = new PlaylistService(fakeRepo);

        // ESECUZIONE
        Playlist result = service.createPlaylist("Rock Classics");

        // VERIFICA
        assertNotNull(result, "Errore: La playlist restituita è null!");
        assertEquals("Rock Classics", result.getName(), "Errore: Il nome della playlist non corrisponde!");
        assertTrue(fakeRepo.isSaveCalled, "Errore: Il PlaylistService non ha chiamato save() sul repository!");
        assertEquals(result, fakeRepo.savedPlaylist, "Errore: La playlist salvata non è quella creata!");
    }

    @Test
    void testErroreNomeDuplicato() {
        // PREPARAZIONE
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        // Forza il repository a dire che il nome "Rock Classics" esiste già!
        fakeRepo.simulateNameExists = true; 
        PlaylistService service = new PlaylistService(fakeRepo);
        
        // VERIFICA: Ci aspettiamo che il service lanci un'eccezione a causa del duplicato
        assertThrows(IllegalArgumentException.class, () -> {
            service.createPlaylist("Rock Classics");
        }, "Ci si aspettava un'IllegalArgumentException a causa del nome della playlist duplicato.");
    }
}