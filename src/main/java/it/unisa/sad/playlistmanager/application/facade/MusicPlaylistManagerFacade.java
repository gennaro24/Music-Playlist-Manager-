package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;

/**
 * Facciata principale del modulo applicativo (Facade Pattern).
 * Fornisce un'interfaccia unificata per isolare la UI dai servizi interni. */
public class MusicPlaylistManagerFacade {

    private final PlaylistService playlistService;

    /**
     * Costruttore con inversione delle dipendenze.
     * @param playlistService Il servizio di coordinamento delle playlist.
     */
    public MusicPlaylistManagerFacade(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Centralizza l'accesso al caso d'uso di creazione di una playlist.
     * @param name Il nome della playlist.
     * @return La playlist creata.
     */
    public Playlist createPlaylist(String name) {
        return this.playlistService.createPlaylist(name);
    }
}