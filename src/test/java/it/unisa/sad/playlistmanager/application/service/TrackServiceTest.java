package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackServiceTest {

    /**
     * 1. CREAZIONE DEL FAKE REPOSITORY
     * Finto database in memoria aggiornato per supportare sia save() che findAll().
     */
    class FakeTrackRepository implements TrackRepository {
        boolean isSaveCalled = false;
        Track trackSavedInDb = null;
        
        // Simula la tabella del database
        List<Track> simulatedDatabase = new ArrayList<>();

        @Override
        public void save(Track track) {
            this.isSaveCalled = true;
            this.trackSavedInDb = track;
            this.simulatedDatabase.add(track); // Salva la traccia nella nostra lista finta
        }

        @Override
        public List<Track> findAll() {
            // Restituisce l'intero "database"
            return this.simulatedDatabase;
        }
    }

    /**
     * 2. IL TEST DI AGGIUNTA
     */
    @Test
    void testAddTrackCreaESalvaCorrettamente() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        Track result = service.addTrack("Bohemian Rhapsody", "Queen", 354, "Rock", 1975);

        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getTitle());
        assertEquals("Queen", result.getAuthor());

        assertTrue(fakeRepo.isSaveCalled, "Errore: Il TrackService non ha chiamato il metodo save() del Repository!");
        assertEquals(result, fakeRepo.trackSavedInDb, "Errore: La traccia salvata non coincide!");
    }

    /**
     * 3. IL TEST DI LETTURA (Nuovo task T-10)
     */
    @Test
    void testGetAllTracksRestituisceIlCatalogo() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        // Prepariamo il finto database inserendo due tracce di prova
        Track track1 = new Track("1", "Song One", "Author", 100, "Pop", 2020);
        Track track2 = new Track("2", "Song Two", "Author", 200, "Rock", 2021);
        fakeRepo.simulatedDatabase.add(track1);
        fakeRepo.simulatedDatabase.add(track2);

        // Chiamiamo il metodo del service che stiamo testando
        List<Track> result = service.getAllTracks();

        // Verifichiamo che il service abbia recuperato correttamente i dati dal repository
        assertNotNull(result, "La lista restituita non dovrebbe essere null");
        assertEquals(2, result.size(), "La lista dovrebbe contenere esattamente 2 tracce");
        assertTrue(result.contains(track1), "La lista deve contenere la prima traccia");
        assertTrue(result.contains(track2), "La lista deve contenere la seconda traccia");
    }
}