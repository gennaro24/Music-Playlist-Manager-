package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackControllerTest {

    /**
     * Inizializza il motore JavaFX in background. 
     * È obbligatorio per poter creare oggetti come TextField o Label nei test JUnit.
     */
    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Il toolkit è già avviato, possiamo ignorare l'eccezione
        }
    }

    /**
     * 1. CREAZIONE DEL FAKE DELLA FACCIATA
     * Questa controfigura non sa nulla di SQLite. Si limita a registrare 
     * se il Controller l'ha chiamata e quali dati le ha passato.
     */
    class FakeFacade extends MusicPlaylistManagerFacade {
        boolean isAddTrackCalled = false;
        String passedTitle;
        String passedAuthor;
        int passedDuration;
        String passedGenre;
        int passedYear;

        // NOTA: Passiamo null al super() perché in questo test non ci servono i Service reali
        public FakeFacade() {
            super(null, null, null, null);
        }

        @Override
        public Track addTrack(String title, String author, int duration, String genre, int year) {
            this.isAddTrackCalled = true;
            this.passedTitle = title;
            this.passedAuthor = author;
            this.passedDuration = duration;
            this.passedGenre = genre;
            this.passedYear = year;
            
            return new Track("dummy-id", title, author, duration, genre, year);
        }

        // Simula ritorni vuoti per evitare errori quando il Controller fa il setup iniziale
        @Override
        public List<Track> getAllTracks() { return new ArrayList<>(); }
        
        @Override
        public List<Playlist> getAllPlaylists() { return new ArrayList<>(); }
    }

    /**
     * 2. IL TEST DEL CONTROLLER
     */
    @Test
    void testAggiuntaTracciaPassaDatiAllaFacciata() throws Exception {
        // PREPARAZIONE
        FakeFacade fakeFacade = new FakeFacade();
        TrackController controller = new TrackController(fakeFacade);
        

        // Tramite Reflection, iniettiamo delle finte caselle di testo nel Controller
        // per simulare l'utente che ha digitato i dati nell'interfaccia.
        injectPrivateField(controller, "txtTitle", new TextField("Bohemian rhapsody"));
        injectPrivateField(controller, "txtAuthor", new TextField("Queen"));
        injectPrivateField(controller, "txtDuration", new TextField("354"));
        injectPrivateField(controller, "txtGenre", new TextField("Rock"));
        injectPrivateField(controller, "txtYear", new TextField("1975"));
        injectPrivateField(controller, "lblFeedback", new Label());
        injectPrivateField(controller, "tableTracks", new TableView<>());


        // ESECUZIONE
        // Chiamiamo il metodo privato handleTrackAddition tramite Reflection
        // per simulare il click sul bottone "Aggiungi"
        Method handleAddMethod = TrackController.class.getDeclaredMethod("handleTrackAddition", ActionEvent.class);
        handleAddMethod.setAccessible(true);
        handleAddMethod.invoke(controller, (ActionEvent) null);

        // VERIFICA
        // Il controller ha passato i dati alla Facciata?
        assertTrue(fakeFacade.isAddTrackCalled, "Errore: Il Controller non ha invocato la Facciata!");
        
        // ATTENZIONE: Il tuo controller applica "toSentenceCase" che trasforma tutto
        // con la prima lettera maiuscola e il resto minuscolo. Verifichiamo che i dati
        // siano arrivati corretti e formattati!
        assertEquals("Bohemian rhapsody", fakeFacade.passedTitle);
        assertEquals("Queen", fakeFacade.passedAuthor);
        assertEquals(354, fakeFacade.passedDuration);
        assertEquals("Rock", fakeFacade.passedGenre);
        assertEquals(1975, fakeFacade.passedYear);
    }

    /**
     * Metodo di utilità per aggirare l'incapsulamento (@FXML private) nei test
     * e iniettare i finti componenti grafici.
     */
    private void injectPrivateField(Object targetObject, String fieldName, Object valueToInject) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, valueToInject);
    }
}