package it.unisa.sad.playlistmanager.bootstrap;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;

/**
 * Interfaccia per la creazione dell'applicazione.
 * 
 */
public interface AppFactory {
    /**
     * metodo che verrrà implementato in SqliteAppFactory
     * @return
     */
    public MusicPlaylistManagerFacade createFacade();
    
}
