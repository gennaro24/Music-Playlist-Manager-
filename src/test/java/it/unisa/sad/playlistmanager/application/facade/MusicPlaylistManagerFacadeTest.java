package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistManagerFacadeTest {

    /**
     * 1. CREAZIONE DEL FAKE OBJECT
     * Creiamo una "controfigura" del TrackService. Invece di eseguire la logica vera
     * (come salvare nel database o validare), questa classe si limita a prendere appunti 
     * su cosa le viene passato, così possiamo controllarlo dopo.
     */
    class FakeTrackService extends TrackService {
        
        // Costruttore: passiamo "null" al vero TrackService per bypassare
        // la richiesta del TrackRepository che non ci serve in questo test.
        public FakeTrackService() {
            super(null); 
        }

        // Variabili "spia" per registrare i dati ricevuti
        boolean isCalled = false;
        String passedTitle;
        String passedAuthor;
        int passedDuration;
        String passedGenre;
        int passedYear;

        @Override
        public Track addTrack(String title, String author, int duration, String genre, int year) {
            // Registriamo che il metodo è stato chiamato e salviamo i parametri
            this.isCalled = true;
            this.passedTitle = title;
            this.passedAuthor = author;
            this.passedDuration = duration;
            this.passedGenre = genre;
            this.passedYear = year;
            
            // Ritorniamo una traccia finta (dummy) per soddisfare la firma del metodo
            return new Track("test-id", title, author, duration, genre, year);
        }
    }

    /**
     * 2. IL TEST VERO E PROPRIO
     */
    @Test
    void testAddTrackDelegaCorrettamente() {
        // Prepariamo la controfigura e la passiamo alla Facade
        FakeTrackService fakeService = new FakeTrackService();
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService);

        // Chiamiamo la Facade con dei dati di prova
        facade.addTrack("Shape of You", "Ed Sheeran", 233, "Pop", 2017);

        // Verifichiamo che la Facade abbia effettivamente girato i dati alla nostra controfigura
        assertTrue(fakeService.isCalled, "Errore: La Facade non ha chiamato il TrackService!");
        assertEquals("Shape of You", fakeService.passedTitle);
        assertEquals("Ed Sheeran", fakeService.passedAuthor);
        assertEquals(233, fakeService.passedDuration);
        assertEquals("Pop", fakeService.passedGenre);
        assertEquals(2017, fakeService.passedYear);
    }
}