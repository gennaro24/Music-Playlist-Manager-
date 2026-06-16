package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Servizio applicativo dedicato alla creazione di playlist automatiche.
 *
 * Questo service dovra coordinare i casi d'uso della US-28:
 * - produrre un'anteprima delle tracce che soddisfano i criteri scelti;
 * - creare una playlist popolata automaticamente con quelle tracce.
 *
 * I criteri arrivano da {@link AutoPlaylistCriteria}. Il service deve
 * interpretarli come filtri combinati: se sono presenti genere, anno e tag, una
 * traccia deve soddisfare tutti i criteri per essere inclusa.
 *
 * La classe e' uno scheletro intenzionale: le task successive completeranno la
 * logica di preview, creazione, validazione dei risultati vuoti e cablaggio in
 * facade/bootstrap.
 * @author Foschillo G. (Solo lo scheletro)
 * @version 1.0
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
     * Implementazione prevista per T3-26:
     * - validare che criteria non sia nullo;
     * - partire da trackService.getAllTracks();
     * - filtrare per genere se criteria.hasGenreCriteria();
     * - filtrare per anno se criteria.hasYearCriteria();
     * - filtrare per tag se criteria.hasTagCriteria();
     * - restituire una lista eventualmente vuota, senza creare playlist.
     *
     * @param criteria criteri della playlist automatica
     * @return tracce corrispondenti ai criteri
     */
    public List<Track> previewAutoPlaylist(AutoPlaylistCriteria criteria) {
        throw new UnsupportedOperationException("TODO T3-26: implementare previewAutoPlaylist.");
    }

    /**
     * Crea una playlist popolata con le tracce corrispondenti ai criteri.
     *
     * Implementazione prevista per T3-30/T3-31:
     * - chiamare previewAutoPlaylist(criteria);
     * - se la preview e' vuota, lanciare ValidationException;
     * - creare la playlist con playlistService.createPlaylist(name);
     * - popolarla con playlistService.populatePlaylist(...);
     * - restituire la playlist creata.
     *
     * @param name nome della playlist da creare
     * @param criteria criteri della playlist automatica
     * @return playlist creata e popolata automaticamente
     */
    public Playlist createAutoPlaylist(String name, AutoPlaylistCriteria criteria) {
        throw new UnsupportedOperationException("TODO T3-30/T3-31: implementare createAutoPlaylist.");
    }
}
