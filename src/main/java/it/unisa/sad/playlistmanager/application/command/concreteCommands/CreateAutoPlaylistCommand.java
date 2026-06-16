package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import java.util.List;

/**
 * TASK SPRINT 3: Comando concreto per supportare l'Undo della creazione
 * di una playlist automatica (US-28).
 */
public class CreateAutoPlaylistCommand implements Command {

    private final String name;
    private final List<Track> matchingTracks;
    private final PlaylistService playlistService;
    private Playlist createdPlaylist;

    public CreateAutoPlaylistCommand(String name, List<Track> matchingTracks, PlaylistService playlistService) {
        this.name = name;
        this.matchingTracks = matchingTracks;
        this.playlistService = playlistService;
    }

    @Override
    public void execute() {
        // 1. Crea la playlist vuota sul database sfruttando il servizio esistente
        createdPlaylist = playlistService.createPlaylist(name);

        // 2. Associa tutte le tracce campionate dall'anteprima
        for (Track track : matchingTracks) {
            playlistService.addTrackToPlaylist(createdPlaylist.getId(), track.getId());
        }
    }

    @Override
    public void undo() {
        if (createdPlaylist == null) {
            return;
        }
        // L'eliminazione della playlist rimuove a cascata anche i record in playlist_tracks
        playlistService.deletePlaylist(createdPlaylist.getId());
        createdPlaylist = null;
    }

    public Playlist getCreatedPlaylist() {
        return createdPlaylist;
    }
}