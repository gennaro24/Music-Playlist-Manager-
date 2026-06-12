# Sprint 2 Task List — Music Playlist Manager

File generato dalla suddivisione task Sprint 2.

## Riepilogo

- Totale task: **111**
- Done: **108**
- To Do: **3**

## Task

| ID Task | User Story | Titolo Task | Assegnata a | Stato |
|---|---|---|---|---|
| T-58 | US-Tech 01 | Creare package bootstrap per centralizzare avvio applicativo e composizione dipendenze | Allocca F. | Done |
| T-59 | US-Tech 01 | Creare AppFactory/ApplicationBootstrapper per istanziare DB manager, repository, service e facade | Allocca F. | Done |
| T-60 | US-Tech 01 | Spostare da MainController ogni creazione diretta di repository, service e facade | Di Marino D. | Done |
| T-61 | US-Tech 01 | Definire ControllerFactory JavaFX per fornire controller configurati con dipendenze | Allocca F. | Done |
| T-62 | US-Tech 01 | Configurare FXMLLoader.setControllerFactory(...) nel bootstrap dell'applicazione | Allocca F. | Done |
| T-63 | US-Tech 01 | Uniformare i controller per ricevere facade tramite costruttore o metodo setFacade(...) | Di Marino D. | Done |
| T-64 | US-Tech 01 | Verificare che nessun controller dipenda direttamente da SQLite o DatabaseConnectionManager | Di Marino D. | Done |
| T-65 | US-Tech 01 | Creare RepositoryException unchecked per incapsulare errori SQL e problemi di persistenza | Foschillo G. | Done |
| T-66 | US-Tech 01 | Aggiornare SqliteTrackRepository per wrappare ogni SQLException in RepositoryException | Foschillo G. | Done |
| T-67 | US-Tech 01 | Aggiornare SqlitePlaylistRepository per wrappare ogni SQLException in RepositoryException | Foschillo G. | Done |
| T-68 | US-Tech 01 | Creare eccezioni applicative minime: ValidationException, TrackNotFoundException, PlaylistNotFoundException | Foschillo G. | Done |
| T-69 | US-Tech 01 | Aggiornare controller per intercettare eccezioni applicative e mostrare messaggi UI coerenti | Allocca F. | Done |
| T-70 | US-Tech 01 | Scrivere test con stub/mock facade per verificare controller testabile senza SQLite | Adinolfi G. | Done |
| T-71 | US-03 | Aggiungere metodo update(Track track) nell'interfaccia TrackRepository | Allocca F. | Done |
| T-72 | US-03 | Implementare SqliteTrackRepository.update(track) con UPDATE tracks SET ... WHERE id = ? | Allocca F. | Done |
| T-73 | US-03 | Gestire update su id inesistente con errore applicativo coerente | Foschillo G. | Done |
| T-74 | US-03 | Creare o riusare Track per input creazione e modifica traccia | Foschillo G. | Done |
| T-75 | US-03 | Estrarre validazione campi traccia in metodo comune riusabile da add/update | Foschillo G. | Done |
| T-76 | US-03 | Implementare TrackService.updateTrack(id, request) senza duplicare logica di addTrack | Foschillo G. | Done |
| T-77 | US-03 | Esporre updateTrack(...) nella MusicPlaylistManagerFacade | Foschillo G. | Done |
| T-78 | US-03 | Aggiornare TrackController per aprire modifica della traccia selezionata | Di Marino D. | Done |
| T-79 | US-03 | Aggiornare UI catalogo dopo modifica senza riavviare l'applicazione | Di Marino D. | Done |
| T-80 | US-03 | Test JUnit: modifica valida aggiorna metadati persistiti | Di Marino D. | Done |
| T-81 | US-03 | Test JUnit: modifica non valida mantiene vecchi valori | Di Marino D. | Done |
| T-82 | US-03 | Test JUnit: traccia in playlist mostra metadati aggiornati senza duplicati | Di Marino D. | Done |
| T-83 | US-04 | Aggiungere metodo deleteById(String id) nell'interfaccia TrackRepository | Allocca F. | Done |
| T-84 | US-04 | Implementare cancellazione riferimenti da playlist_tracks per la traccia eliminata | Allocca F. | Done |
| T-85 | US-04 | Implementare SqliteTrackRepository.deleteById(id) con controllo righe eliminate | Allocca F. | Done |
| T-86 | US-04 | Implementare TrackService.deleteTrack(id) verificando esistenza traccia prima della cancellazione | Foschillo G. | Done |
| T-87 | US-04 | Gestire nel PlaybackService il caso in cui la traccia eliminata sia quella corrente | Foschillo G. | Done |
| T-88 | US-04 | Esporre deleteTrack(id) nella facade | Foschillo G. | Done |
| T-89 | US-04 | Aggiungere dialogo di conferma eliminazione nel TrackController | Di Marino D. | Done |
| T-90 | US-04 | Aggiornare catalogo e playlist visualizzate dopo eliminazione traccia | Di Marino D. | Done |
| T-91 | US-04 | Test JUnit: eliminazione confermata rimuove traccia dal catalogo | Di Marino D. | Done |
| T-92 | US-04 | Test JUnit: eliminazione rimuove riferimenti da playlist_tracks | Di Marino D. | Done |
| T-93 | US-04 | Test JUnit: eliminazione id inesistente produce errore controllato | Di Marino D. | Done |
| T-94 | US-5.1 | Aggiungere metodo deleteById(String playlistId) nell'interfaccia PlaylistRepository | Allocca F. | Done |
| T-95 | US-5.1 | Implementare cancellazione record associati da playlist_tracks per playlist eliminata | Allocca F. | Done |
| T-96 | US-5.1 | Implementare SqlitePlaylistRepository.deleteById(playlistId) | Allocca F. | Done |
| T-97 | US-5.1 | Implementare PlaylistService.deletePlaylist(id) verificando esistenza playlist | Foschillo G. | Done |
| T-98 | US-5.1 | Gestire nel playback il caso in cui venga eliminata la playlist in riproduzione | Foschillo G. | Done |
| T-99 | US-5.1 | Esporre deletePlaylist(id) nella facade | Foschillo G. | Done |
| T-100 | US-5.1 | Aggiungere pulsante o azione Elimina playlist nella schermata playlist | Di Marino D. | Done |
| T-101 | US-5.1 | Aggiungere dialogo di conferma nel PlaylistController | Di Marino D. | Done |
| T-102 | US-5.1 | Aggiornare lista playlist e dettaglio playlist dopo eliminazione | Di Marino D. | Done |
| T-103 | US-5.1 | Test JUnit: eliminazione playlist rimuove playlist e associazioni | Di Marino D. | Done |
| T-104 | US-5.1 | Test JUnit: eliminazione playlist non elimina tracce dal catalogo | Di Marino D. | Done |
| T-105 | US-10 | Verificare e rifinire PlaybackService.pause() per traccia singola in stato PLAYING | Foschillo G. | Done |
| T-106 | US-10 | Gestire pause() come no-op sicuro se stato già PAUSED | Foschillo G. | Done |
| T-107 | US-10 | Aggiornare PlaybackSnapshot con currentTrack, state, mode, elapsedSeconds | Foschillo G. | Done |
| T-108 | US-10 | Implementare metodo testabile tick() per avanzamento simulato del tempo | Foschillo G. | Done |
| T-109 | US-10 | Bloccare incremento elapsedSeconds quando stato è PAUSED | Foschillo G. | Done |
| T-110 | US-10 | Aggiornare UI playback con stato PAUSED e tempo congelato | Adinolfi G. | Done |
| T-111 | US-10 | Test JUnit: pausa mantiene traccia corrente e stato PAUSED | Adinolfi G. | Done |
| T-112 | US-10 | Test JUnit: tick() non avanza quando player è PAUSED | Adinolfi G. | Done |
| T-113 | US-12 | Definire coda playback: lista tracce, indice corrente, sorgente SINGLE/PLAYLIST/CATALOG | Adinolfi G. | Done |
| T-114 | US-12 | Implementare SequentialPlaybackStrategy per calcolare prossima traccia in ordine | Adinolfi G. | Done |
| T-115 | US-12 | Implementare PlaybackService.playPlaylist(playlistId) caricando tracce dal repository | Foschillo G. | Done |
| T-116 | US-12 | Impedire avvio playback su playlist vuota con eccezione applicativa controllata | Foschillo G. | Done |
| T-117 | US-12 | Esporre playPlaylist(playlistId) nella facade | Foschillo G. | Done |
| T-118 | US-12 | Collegare azione Play playlist nella UI playlist | Adinolfi G. | Done |
| T-119 | US-12 | Test JUnit: playlist popolata parte dalla prima traccia | Adinolfi G. | Done |
| T-120 | US-12 | Test JUnit: playlist vuota non avvia il playback | Adinolfi G. | Done |
| T-121 | US-13 | Estendere PlaybackContext/Player per conservare indice corrente della playlist | Foschillo G. | Done |
| T-122 | US-13 | Rendere pause() unica per traccia singola e playlist, evitando metodi duplicati | Foschillo G. | Done |
| T-123 | US-13 | Verificare che pausa playlist mantenga indice e traccia corrente | Adinolfi G. | Done |
| T-124 | US-13 | Aggiornare UI pulsante Pause per qualsiasi sorgente di playback attiva | Adinolfi G. | Done |
| T-125 | US-13 | Test JUnit: pausa playlist conserva indice corrente e stato PAUSED | Adinolfi G. | Done |
| T-126 | US-14 | Aggiungere metodo skipToNext() in PlaybackService | Di Marino D. | Done |
| T-127 | US-14 | Delegare alla strategia attiva il calcolo della prossima traccia | Di Marino D. | Done |
| T-128 | US-14 | Gestire skip con traccia successiva mantenendo PLAYING | Di Marino D. | Done |
| T-129 | US-14 | Gestire skip dall'ultima traccia sequential portando stato a STOPPED | Di Marino D. | Done |
| T-130 | US-14 | Gestire skip in stato PAUSED mantenendo PAUSED dopo cambio traccia | Di Marino D. | Done |
| T-131 | US-14 | Esporre skipToNext() nella facade | Di Marino D. | Done |
| T-132 | US-14 | Collegare pulsante Skip nella PlaybackView | Foschillo G. | Done |
| T-133 | US-14 | Test JUnit: skip normale, skip ultima traccia, skip da pausa | Di Marino D. | Done |
| Test JUnit: cambio da loop singolo a sequential non resetta traccia corrente | US-11 | Implementare modalità REPEAT_ONE/single track loop nel PlaybackMode se non già presente | Allocca F. | Done |
| T-135 | US-11 | Implementare fine traccia in loop: stessa traccia, elapsedSeconds a 0, stato PLAYING | Allocca F. | Done |
| T-136 | US-11 | Esporre cambio modalità single-track-loop tramite PlaybackService e facade | Allocca F. | Done |
| T-137 | US-11 | Collegare controllo UI per attivare/disattivare loop traccia | Allocca F. | Done |
| T-138 | US-11 | Test JUnit: fine traccia in loop singolo riparte dalla stessa traccia | Allocca F. | Done |
| T-139 | US-11 | Test JUnit: cambio da loop singolo a sequential non resetta traccia corrente | Allocca F. | Done |
| T-140 | US-15 | Implementare ShufflePlaybackStrategy usando coda mescolata e non random a ogni skip | Adinolfi G. | Done |
| T-141 | US-15 | Prevedere dipendenza Random iniettabile per rendere test shuffle ripetibili | Adinolfi G. | Done |
| T-142 | US-15 | Gestire playlist con una sola traccia senza errori | Adinolfi G. | Done |
| T-143 | US-15 | Aggiornare PlaybackService.setMode(SHUFFLE) senza interrompere traccia corrente | Adinolfi G. | Done |
| T-144 | US-15 | Esporre modalità shuffle nella facade | Adinolfi G. | Done |
| T-145 | US-15 | Collegare selettore/pulsante Shuffle nella PlaybackView | Adinolfi G. | Done |
| T-146 | US-15 | Test JUnit: shuffle produce ordine valido contenente tutte le tracce una volta | Adinolfi G. | Done |
| T-147 | US-15 | Test JUnit: shuffle monotraccia resta stabile | Adinolfi G. | Done |
| T-148 | US-16 | Implementare modalità REPEAT_ALL/playlist loop nella strategia di avanzamento | Allocca F. | Done |
| T-149 | US-16 | Gestire skip dall'ultima traccia in loop tornando alla prima traccia | Allocca F. | Done |
| T-150 | US-16 | Gestire fine naturale dell'ultima traccia in loop tornando alla prima traccia | Allocca F. | Done |
| T-151 | US-16 | Implementare cambio modalità a caldo senza resettare traccia e tempo corrente | Allocca F. | Done |
| T-152 | US-16 | Esporre loop playlist nella facade | Allocca F. | Done |
| T-153 | US-16 | Collegare controllo Loop Playlist nella PlaybackView | Allocca F. | Done |
| T-154 | US-16 | Test JUnit: loop playlist torna alla prima traccia dopo l'ultima | Allocca F. | Done |
| T-155 | US-16 | Test JUnit: cambio modalità da loop a sequential non interrompe traccia corrente | Allocca F. | Done |
| T-156 | US-17 | Definire PlaybackSnapshot come unico DTO letto dalla UI per stato player | Di Marino D. | Done |
| T-157 | US-17 | Aggiornare PlaybackService.getSnapshot() dopo play, pause, skip, stop e cambio modalità | Di Marino D. | Done |
| T-158 | US-17 | Implementare notifica UI semplice: callback/listener oppure refresh esplicito dopo ogni comando | Foschillo G. | Done |
| T-159 | US-17 | Aggiornare PlaybackView per mostrare traccia corrente, stato, modalità e tempo simulato | Foschillo G. | Done |
| T-160 | US-17 | Gestire stato STOPPED nella UI con placeholder coerente | Foschillo G. | Done |
| T-161 | US-17 | Test JUnit: snapshot dopo play mostra PLAYING e traccia corrente | Di Marino D. | Done |
| T-162 | US-17 | Test JUnit: snapshot dopo pause mostra PAUSED e mantiene traccia corrente | Di Marino D. | Done |
| T-163 | US-17 | Test JUnit: snapshot dopo stop/termine coda mostra STOPPED coerente | Di Marino D. | Done |
| T-164 | Sprint 2 QA | Eseguire test di regressione completo su US Sprint 1 e playback base | Foschillo G. | Done |
| T-165 | Sprint 2 QA | Eseguire mvn clean test prima delle PR finali | Foschillo G. | Done |
| T-166 | Sprint 2 Docs | Aggiornare Architecture.md con bootstrap, controller factory, eccezioni e strategie playback | Foschillo G. | To Do |
| T-167 | Sprint 2 Docs | Aggiornare docs/Sprint2/SprintBacklog.md con task definitive e assegnazioni | Foschillo G. | Done |
| T-168 | Sprint 2 Docs | Aggiornare burndown, review report e retrospective report di Sprint 2 | Foschillo G. | Done |

## Riepilogo per assegnatario

| Assegnata a | Numero task |
|---|---:|
| Adinolfi G. | 20 |
| Allocca F. | 27 |
| Di Marino D. | 30 |
| Foschillo G. | 34 |

## Riepilogo per User Story

| User Story | Numero task |
|---|---:|
| Sprint 2 Docs | 3 |
| Sprint 2 QA | 2 |
| US-03 | 12 |
| US-04 | 11 |
| US-10 | 8 |
| US-11 | 6 |
| US-12 | 8 |
| US-13 | 5 |
| US-14 | 8 |
| US-15 | 8 |
| US-16 | 8 |
| US-17 | 8 |
| US-5.1 | 11 |
| US-Tech 01 | 13 |