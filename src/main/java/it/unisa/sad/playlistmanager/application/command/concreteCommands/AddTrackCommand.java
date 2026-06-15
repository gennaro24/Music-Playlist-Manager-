package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Aggiunge una traccia al catalogo e conserva l'entita' creata per poter
 * annullare l'operazione usando lo stesso identificativo.
 *
 * Viene creato da CommandFactory e invocato da UndoManager quando la facade
 * riceve una richiesta di aggiunta globale di una traccia.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class AddTrackCommand implements Command {

    private final TrackService trackService;
    private final String title;
    private final String author;
    private final int duration;
    private final String genre;
    private final int year;

    private Track createdTrack;

    public AddTrackCommand(
            TrackService trackService,
            String title,
            String author,
            int duration,
            String genre,
            int year) {
        if (trackService == null) {
            throw new IllegalArgumentException("TrackService cannot be null");
        }

        this.trackService = trackService;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.genre = genre;
        this.year = year;
    }

    @Override
    public void execute() {
        createdTrack = trackService.addTrack(title, author, duration, genre, year);
    }

    @Override
    public void undo() {
        if (createdTrack == null) {
            return;
        }

        trackService.deleteTrack(createdTrack.getId());
        // Dopo un undo riuscito il command non deve eliminare di nuovo la traccia.
        createdTrack = null;
    }

    public Track getCreatedTrack() {
        return createdTrack;
    }
}
