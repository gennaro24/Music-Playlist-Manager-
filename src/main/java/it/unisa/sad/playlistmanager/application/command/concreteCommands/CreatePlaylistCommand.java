package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;

/**
 * Crea una playlist e conserva l'entita' restituita dal service.
 * L'undo usa l'identificativo effettivamente assegnato alla playlist.
 *
 * Viene creato da CommandFactory quando la facade richiede la creazione di una
 * playlist ed e' eseguito e registrato da UndoManager.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class CreatePlaylistCommand implements Command {

    private final String name;
    private final PlaylistService playlistService;

    private Playlist createdPlaylist;

    public CreatePlaylistCommand(String name, PlaylistService playlistService) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Playlist name cannot be null or empty");
        }
        if (playlistService == null) {
            throw new IllegalArgumentException("PlaylistService cannot be null");
        }

        this.name = name;
        this.playlistService = playlistService;
    }

    @Override
    public void execute() {
        createdPlaylist = playlistService.createPlaylist(name);
    }

    @Override
    public void undo() {
        if (createdPlaylist == null) {
            return;
        }

        playlistService.deletePlaylist(createdPlaylist.getId());
        createdPlaylist = null;
    }

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }
}
