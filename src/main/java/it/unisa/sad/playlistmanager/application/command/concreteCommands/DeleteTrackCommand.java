package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Elimina una traccia dal catalogo conservando sia l'entita' sia la posizione
 * occupata in ogni playlist. L'undo ripristina prima la traccia e poi tutte le
 * associazioni.
 *
 * Viene creato da CommandFactory quando la facade richiede una cancellazione
 * globale ed e' eseguito da UndoManager. Il playback viene fermato dalla
 * facade dopo la cancellazione riuscita e non viene ripreso durante l'undo.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class DeleteTrackCommand implements Command {

    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final String trackId;

    private final Map<String, Integer> playlistPositions = new LinkedHashMap<>();
    private Track deletedTrack;

    public DeleteTrackCommand(
            TrackService trackService,
            PlaylistService playlistService,
            String trackId) {
        if (trackService == null) {
            throw new IllegalArgumentException("TrackService cannot be null");
        }
        if (playlistService == null) {
            throw new IllegalArgumentException("PlaylistService cannot be null");
        }
        if (trackId == null || trackId.isBlank()) {
            throw new IllegalArgumentException("TrackId cannot be null or empty");
        }

        this.trackService = trackService;
        this.playlistService = playlistService;
        this.trackId = trackId;
    }

    @Override
    public void execute() {
        deletedTrack = trackService.getTrackById(trackId);
        playlistPositions.clear();

        // Le associazioni vengono salvate prima della delete globale.
        for (Playlist playlist : playlistService.getAllPlaylists()) {
            playlistService.getTrackPosition(playlist.getId(), trackId)
                    .ifPresent(position ->
                            playlistPositions.put(playlist.getId(), position));
        }

        trackService.deleteTrack(trackId);
    }

    @Override
    public void undo() {
        if (deletedTrack == null) {
            return;
        }

        /*
         * Il repository esegue il ripristino della traccia e delle associazioni
         * come una sola operazione atomica. In caso di errore UndoManager
         * conserva il command nella cronologia per consentire un nuovo tentativo.
         */
        trackService.restoreTrack(deletedTrack, playlistPositions);

        // Lo snapshot viene eliminato soltanto dopo il commit riuscito.
        deletedTrack = null;
        playlistPositions.clear();
    }

    public Track getDeletedTrack() {
        return deletedTrack;
    }
}
