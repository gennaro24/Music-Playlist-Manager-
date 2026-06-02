package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test d'unità per la facciata applicativa {@link MusicPlaylistManagerFacade}.
 * Verifica il corretto funzionamento del Facade Pattern assicurando che le chiamate
 * provenienti dal Presentation Layer siano delegate senza alterazioni ai rispettivi servizi.
 * Utilizza una classe Fake interna per isolare i test dalle reali logiche di business.
 * * @version 1.1
 */
class MusicPlaylistManagerFacadeTest {

    /**
     * Controfigura d'oggetto (Fake Object) di {@link TrackService}.
     * Sovrascrive i metodi del servizio reale per tracciare l'avvenuta delega
     * ed evitare di interagire con i repository di persistenza o il database reale.
     */
    class FakeTrackService extends TrackService {
        
        public FakeTrackService() {
            super(null); 
        }

        // Variabili stato "spia" per verificare l'invocazione dei metodi
        boolean isAddTrackCalled = false;
        boolean isGetAllTracksCalled = false;
        
        // Catalogo finto pre-popolato restituito dal metodo stub
        List<Track> dummyCatalog = Arrays.asList(
            new Track("1", "Song One", "Artist", 100, "Pop", 2020),
            new Track("2", "Song Two", "Artist", 200, "Rock", 2021)
        );

        @Override
        public Track addTrack(String title, String author, int duration, String genre, int year) {
            this.isAddTrackCalled = true;
            return new Track("test-id", title, author, duration, genre, year);
        }

        @Override
        public List<Track> getAllTracks() {
            this.isGetAllTracksCalled = true;
            return dummyCatalog;
        }
    }

    /**
     * Verifica che la Facade deleghi correttamente l'aggiunta di una traccia
     * al relativo metodo esposto da TrackService.
     */
    @Test
    void testAddTrackDelegaCorrettamente() {
        // Prepariamo la controfigura del TrackService
        FakeTrackService fakeService = new FakeTrackService();
        
        // Passiamo null come secondo parametro (PlaylistService) poiché non è oggetto di questo test
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService, null);

        // Invochiamo il metodo sulla Facade
        facade.addTrack("Shape of You", "Ed Sheeran", 233, "Pop", 2017);

        // Asserzione: verifichiamo se il metodo del servizio finto è stato effettivamente toccato
        assertTrue(fakeService.isAddTrackCalled, "Errore: La Facade non ha chiamato addTrack() del TrackService!");
    }

    /**
     * Verifica che la Facade deleghi correttamente il recupero dell'intero catalogo
     * musicale al metodo getAllTracks() del TrackService.
     */
    @Test
    void testGetAllTracksDelegaCorrettamente() {
        // Prepariamo la controfigura del TrackService
        FakeTrackService fakeService = new FakeTrackService();
        
        // Inserito "null" come secondo parametro per soddisfare il costruttore a due vie della Facade
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService, null);

        // Chiamiamo il metodo della Facade sotto analisi
        List<Track> result = facade.getAllTracks();

        // Verifichiamo che abbia delegato correttamente la chiamata al servizio
        assertTrue(fakeService.isGetAllTracksCalled, "Errore: La Facade non ha chiamato getAllTracks() del TrackService!");
        
        // Verifichiamo che la lista ritornata non sia corrotta ed equivalga a quella del servizio
        assertEquals(fakeService.dummyCatalog, result, "Errore: La lista restituita non è quella del TrackService!");
    }
}