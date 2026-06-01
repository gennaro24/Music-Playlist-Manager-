package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

class TrackServiceTest {

    /**
     * 1. CREAZIONE DEL FAKE REPOSITORY
     * Creiamo un finto database in memoria. Invece di salvare su file o su SQL,
     * si limita a segnarsi se il metodo "save" è stato chiamato e quale traccia
     * gli è stata passata.
     */
    class FakeTrackRepository implements TrackRepository {
        boolean isSaveCalled = false;
        Track trackSavedInDb = null;

        @Override
        public void save(Track track) {
            this.isSaveCalled = true;
            this.trackSavedInDb = track;
        }
        /**
         * 
         * DA IMPLEMENTARE
         */
        @Override
        public Optional<Track> findById(String id){
            return Optional.empty();
        }

        @Override
        public List<Track> findAll(){
            return null;
        }

        // NOTA: Se la tua interfaccia TrackRepository ha altri metodi (es. findAll, delete),
        // il tuo IDE (VS Code) ti chiederà di aggiungerli qui per rispettare l'interfaccia. 
        // Se succede, aggiungili pure lasciandoli completamente vuoti o facendogli ritornare null,
        // tanto in questo test ci interessa solo il metodo save!
    }

    /**
     * 2. IL TEST VERO E PROPRIO
     */
    @Test
    void testAddTrackCreaESalvaCorrettamente() {
        // PREPARAZIONE (Arrange)
        // Creiamo il database finto e lo passiamo al vero TrackService
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        // ESECUZIONE (Act)
        // Chiamiamo il metodo del service
        Track result = service.addTrack("Bohemian Rhapsody", "Queen", 354, "Rock", 1975);

        // VERIFICA (Assert)
        // 1. Controlliamo che il service ci abbia restituito la traccia corretta
        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getTitle());
        assertEquals("Queen", result.getAuthor());

        // 2. Controlliamo che il service abbia effettivamente detto al database di salvare!
        assertTrue(fakeRepo.isSaveCalled, "Errore: Il TrackService non ha chiamato il metodo save() del Repository!");
        
        // 3. Controlliamo che la traccia che il service ha tentato di salvare sia esattamente quella creata
        assertEquals(result, fakeRepo.trackSavedInDb, "Errore: La traccia salvata nel DB non coincide con quella creata!");
    }
}