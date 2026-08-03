package it.unisa.sad.playlistmanager.application.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Servizio applicativo dedicato alla creazione di playlist automatiche.
 *
 * Coordina i casi d'uso della US-28:
 * - produrre un'anteprima delle tracce che soddisfano i criteri scelti;
 * - creare una playlist popolata automaticamente con quelle tracce.
 *
 * I criteri arrivano da {@link AutoPlaylistCriteria} e vengono interpretati come
 * filtri combinati in AND: se sono presenti genere, anno e tag, una traccia deve
 * soddisfarli tutti per essere inclusa.
 *
 * @author Foschillo G. (scheletro), Adinolfi G. (previewAutoPlaylist)
 * @version 1.1
 */
public class AutoPlaylistService {

    private final TrackService trackService;
    private final TagService tagService;
    private final PlaylistService playlistService;

    public AutoPlaylistService(
            TrackService trackService,
            TagService tagService,
            PlaylistService playlistService) {
        this.trackService = trackService;
        this.tagService = tagService;
        this.playlistService = playlistService;
    }

    /**
     * Restituisce le tracce del catalogo che soddisfano i criteri indicati.
     *
     * Parte dall'intero catalogo e applica in sequenza i filtri attivi (genere,
     * anno, tag), interpretati come condizioni combinate in AND. Il filtro per
     * genere e' case-insensitive. Non crea alcuna playlist: si limita a calcolare
     * l'anteprima e puo' restituire una lista vuota se nessuna traccia
     * corrisponde ai criteri.
     *
     * @param criteria criteri della playlist automatica
     * @return tracce corrispondenti ai criteri, eventualmente lista vuota
     * @throws ValidationException se criteria e' nullo
     */
    public List<Track> previewAutoPlaylist(AutoPlaylistCriteria criteria) {
        if (criteria == null) {
            throw new ValidationException("I criteri della playlist automatica non possono essere nulli.");
        }

        //recupero tutte le tracce
        List<Track> tracks = trackService.getAllTracks();

        //filtro per genere
        if (criteria.hasGenreCriteria()) {
            String genre = criteria.getGenre();
            tracks = tracks.stream()
                    .filter(track -> genre.equalsIgnoreCase(track.getGenre()))
                    .collect(Collectors.toList());
        }

        //filtro per anno
        if (criteria.hasYearCriteria()) {
            int year = criteria.getYear();
            tracks = tracks.stream()
                    .filter(track -> track.getYear() == year)
                    .collect(Collectors.toList());
        }

        //filtro per tag
        if (criteria.hasTagCriteria()) {
            Set<String> taggedTrackIds = tagService.getTracksByTag(criteria.getTagId())
                    .stream()
                    .map(Track::getId)
                    .collect(Collectors.toSet());
            tracks = tracks.stream()
                    .filter(track -> taggedTrackIds.contains(track.getId()))
                    .collect(Collectors.toList());
        }

        //restituisco le tracce filtrate
        return tracks;
    }

    /**
     * TASK T3-30 & T3-31: Crea una playlist popolata con le tracce corrispondenti ai criteri.
     *
     * @param name nome della playlist da creare
     * @param criteria criteri della playlist automatica
     * @return playlist creata e popolata automaticamente
     * @throws ValidationException se il nome è vuoto o se non ci sono tracce corrispondenti
     */
    public Playlist createAutoPlaylist(String name, AutoPlaylistCriteria criteria) {
        // Validazione formale del nome inserito dall'utente
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Il nome della playlist automatica è obbligatorio.");
        }

        // 1. Sfrutta il metodo di filtraggio in AND sviluppato da Adinolfi (T3-26)
        List<Track> matchingTracks = previewAutoPlaylist(criteria);

        // 2. TASK T3-31: Blocca la creazione tramite eccezione se l'anteprima produce 0 risultati
        if (matchingTracks == null || matchingTracks.isEmpty()) {
            throw new ValidationException("Nessuna traccia soddisfa i criteri scelti. Impossibile creare una playlist vuota.");
        }

        // 3. TASK T3-30: Creazione fisica della playlist tramite il PlaylistService esistente
        Playlist autoPlaylist = playlistService.createPlaylist(name);

        // 4. Associazione in sequenza di tutte le tracce filtrate all'interno della nuova playlist
        for (Track track : matchingTracks) {
            playlistService.addTrackToPlaylist(autoPlaylist.getId(), track.getId());
        }

        return autoPlaylist;
    }
}