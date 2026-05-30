package it.unisa.sad.playlistmanager.ui.controller;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;

public class PlaylistController {


    private MusicPlaylistManagerFacade facade;

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }
}
