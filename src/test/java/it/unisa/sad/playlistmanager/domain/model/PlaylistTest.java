package it.unisa.sad.playlistmanager.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    // --- TEST COSTRUTTORE E NOME ---

    @Test
    void testCreazionePlaylistValida() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        
        assertEquals("p-1", playlist.getId());
        assertEquals("Rock Classics", playlist.getName());
        assertTrue(playlist.getTracks().isEmpty(), "La playlist appena creata dovrebbe essere vuota");
    }

    @Test
    void testErroreNomeMancanteCostruttore() {
        // Nome null
        assertThrows(IllegalArgumentException.class, () -> {
            new Playlist("p-1", null);
        });

        // Nome vuoto o solo spazi
        assertThrows(IllegalArgumentException.class, () -> {
            new Playlist("p-1", "   ");
        });
    }

    // --- TEST SETTER NOME ---

    @Test
    void testModificaNomeValida() {
        Playlist playlist = new Playlist("p-1", "Vecchi Successi");
        playlist.setName("Nuovi Successi");
        
        assertEquals("Nuovi Successi", playlist.getName());
    }

    @Test
    void testErroreNomeMancanteSetter() {
        Playlist playlist = new Playlist("p-1", "Playlist Valida");
        
        assertThrows(IllegalArgumentException.class, () -> {
            playlist.setName(null);
        });
    }

    // --- TEST AGGIUNTA TRACCE ---

    @Test
    void testAggiuntaTracciaValida() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        Track track = new Track("t-1", "Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        
        playlist.addTrack(track);
        
        assertEquals(1, playlist.getTracks().size());
        assertEquals(track, playlist.getTracks().get(0));
    }

    @Test
    void testErroreAggiuntaTracciaNulla() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        
        assertThrows(IllegalArgumentException.class, () -> {
            playlist.addTrack(null);
        });
    }

    // --- TEST INCAPSULAMENTO ---

    @Test
    void testListaTracceImmodificabile() {
        Playlist playlist = new Playlist("p-1", "Rock Classics");
        Track track = new Track("t-1", "Bohemian Rhapsody", "Queen", 354, "Rock", 1975);
        
        // Verifichiamo che se qualcuno cerca di forzare l'inserimento aggirando il metodo addTrack(),
        // la Collections.unmodifiableList lanci l'eccezione corretta bloccandolo.
        assertThrows(UnsupportedOperationException.class, () -> {
            playlist.getTracks().add(track);
        });
    }
}