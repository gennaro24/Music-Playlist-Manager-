# Pull Request - Completamento task T3-01 / T3-09

## Titolo suggerito

```text
feat: introduce Command pattern e undo per catalogo e playlist
```

## Descrizione

Questa Pull Request completa le task da **T3-01 a T3-09**, introducendo il
meccanismo applicativo di Undo per le principali operazioni di modifica del
catalogo e delle playlist.

Il lavoro copre il nucleo applicativo delle User Story:

- **US-19** - Undo inserimento di una nuova traccia nel catalogo;
- **US-20** - Undo eliminazione globale di una traccia;
- **US-21** - Undo creazione di una playlist;
- **US-22** - Undo eliminazione di una playlist;
- **US-23** - Undo aggiunta di una traccia a una playlist;
- **US-24** - Undo rimozione di una traccia da una playlist.

L'architettura MVC a layer non e stata sostituita. Nel layer applicativo e
stato aggiunto il **Command pattern**, mantenendo i controller dipendenti dal
solo `MusicPlaylistManagerFacade` e lasciando l'accesso alla persistenza ai
service e ai repository.

## Task completate

| Task | Risultato incluso nella PR | Stato |
|---|---|---|
| **T3-01** | Definizione del contratto comune `Command` con operazioni `execute()` e `undo()` | Completata |
| **T3-02** | Implementazione di `UndoManager` con cronologia globale LIFO in memoria | Completata |
| **T3-03** | Implementazione di `CommandFactory` per la costruzione centralizzata dei command | Completata |
| **T3-04** | Undo della creazione e dell'eliminazione globale delle tracce | Completata |
| **T3-05** | Undo della creazione e dell'eliminazione delle playlist | Completata |
| **T3-06** | Undo dell'aggiunta e della rimozione di tracce dalle playlist | Completata |
| **T3-07** | Supporto applicativo e persistente per salvare e ripristinare le posizioni delle tracce | Completata |
| **T3-08** | Integrazione dei command nel facade e nel bootstrap applicativo | Completata |
| **T3-09** | Aggiornamento della documentazione architetturale e adeguamento dei test esistenti | Completata |

## Modifiche principali

### Infrastruttura Command

E stato aggiunto il package `application.command`, composto da:

- `Command`, contratto comune delle operazioni annullabili;
- `UndoManager`, responsabile dell'esecuzione e della cronologia LIFO;
- `CommandFactory`, responsabile della creazione dei command concreti;
- package `concreteCommands`, contenente le implementazioni dei casi d'uso.

`UndoManager`:

- esegue un command prima di inserirlo nella cronologia;
- registra solamente command eseguiti con successo;
- annulla per prima l'ultima operazione eseguita;
- rimuove il command dalla cronologia solamente dopo un undo riuscito;
- espone `canUndo()` per verificare la disponibilita di operazioni annullabili.

### Command concreti

Sono stati implementati:

- `AddTrackCommand`;
- `DeleteTrackCommand`;
- `CreatePlaylistCommand`;
- `DeletePlaylistCommand`;
- `AddTrackToPlaylistCommand`;
- `RemoveTrackFromPlaylistCommand`.

Ogni command conserva solamente lo stato necessario per ripristinare
l'operazione:

- entita creata, per annullarne la creazione;
- entita eliminata, per ricrearla mantenendo ID e metadati;
- elenco ordinato delle tracce, per ricostruire una playlist eliminata;
- associazioni playlist-traccia e posizioni originali, per ripristinare una
  traccia eliminata globalmente;
- posizione originale della traccia, per annullare la rimozione da una
  playlist.

### Undo delle tracce

L'aggiunta globale di una traccia viene ora eseguita tramite
`AddTrackCommand`. L'undo elimina la traccia appena creata utilizzando il suo
ID effettivo.

L'eliminazione globale viene gestita da `DeleteTrackCommand`, che prima della
cancellazione salva:

- tutti i metadati della traccia;
- gli ID delle playlist in cui era presente;
- la posizione occupata in ciascuna playlist.

L'undo ricrea la traccia con lo stesso ID e ripristina tutte le associazioni
nelle rispettive posizioni originali.

### Undo delle playlist

La creazione di una playlist viene eseguita tramite
`CreatePlaylistCommand`. L'undo elimina la playlist usando l'ID assegnato
durante la creazione.

`DeletePlaylistCommand` salva prima della cancellazione:

- ID e nome della playlist;
- copia ordinata delle tracce contenute.

L'undo ricrea la playlist con lo stesso ID e ripristina le tracce nello stesso
ordine.

### Undo delle associazioni playlist-traccia

`AddTrackToPlaylistCommand` rende annullabile l'aggiunta di una traccia a una
playlist. L'undo elimina solamente l'associazione, senza rimuovere la traccia
dal catalogo.

`RemoveTrackFromPlaylistCommand` salva la posizione originale prima della
rimozione. L'undo reinserisce la traccia esattamente nella posizione
precedente.

### Facade

`MusicPlaylistManagerFacade` e stato aggiornato per instradare tramite command
le seguenti operazioni:

- `addTrack(...)`;
- `deleteTrack(...)`;
- `createPlaylist(...)`;
- `deletePlaylist(...)`;
- `addTrackToPlaylist(...)`;
- `removeTrackFromPlaylist(...)`.

Sono stati aggiunti:

- `undoLastAction()`;
- `canUndo()`.

Le operazioni di lettura, aggiornamento dei metadati e playback continuano a
delegare direttamente ai rispettivi service.

La gestione degli effetti delle eliminazioni sul playback resta nel facade.
L'undo ripristina i dati persistenti, ma non riprende il playback precedente.

### Service

`TrackService` supporta ora la ricreazione di una traccia con un ID specifico,
necessaria per l'undo di una cancellazione globale.

`PlaylistService` e stato esteso con operazioni per:

- creare una playlist con un ID specifico;
- ripopolare una playlist;
- verificare la presenza di una traccia;
- recuperare la posizione di una traccia;
- ripristinare una traccia in una posizione specifica.

I command continuano a dipendere dai service applicativi e non accedono
direttamente ai repository SQLite.

### Domain model

`Playlist` dispone di un costruttore completo che permette di inizializzare
ID, nome e copia della lista di tracce.

Questo supporta la ricostruzione dello stato senza condividere direttamente
una collezione modificabile esterna.

### Repository e SQLite

Il contratto `PlaylistRepository` e stato esteso con:

- `getTrackPosition(...)`;
- `addTrackToPlaylistAtPosition(...)`.

`SqlitePlaylistRepository` implementa:

- il recupero della posizione dalla tabella `playlist_tracks`;
- il reinserimento di un'associazione playlist-traccia in una posizione
  specifica;
- la conversione degli errori SQL in `RepositoryException`.

Queste operazioni permettono di conservare l'ordine delle playlist durante
l'undo.

### Bootstrap e dependency injection

`SqliteAppFactory` crea ora:

- i service condivisi;
- una `CommandFactory` configurata con `TrackService` e `PlaylistService`;
- un unico `UndoManager` per la sessione;
- il facade completo con tutte le dipendenze.

La cronologia Undo e quindi condivisa da tutte le operazioni eseguite nella
stessa sessione applicativa.

### Test

I test esistenti sono stati adeguati alle nuove dipendenze e ai nuovi metodi
del repository.

In particolare:

- il test del facade costruisce `CommandFactory` e `UndoManager`;
- i fake repository implementano i nuovi metodi richiesti dall'interfaccia;
- gli stub non ancora coperti da test specifici sono marcati con `TODO`.

## Documentazione

E stato aggiornato `docs/PreGame/Architecture.md` con:

- il nuovo package `application.command`;
- il flusso delle mutazioni annullabili;
- il flusso di undo;
- il nuovo ciclo di dependency injection;
- il ruolo di facade, factory, manager e command;
- i limiti della cronologia in memoria.

E stato aggiunto `docs/Sprint3/Guidelines.md`, che documenta:

- il funzionamento del Command pattern nel progetto;
- le responsabilita dei componenti;
- lo stato conservato da ogni command;
- il collegamento con service e repository;
- le regole per aggiungere nuovi command;
- i limiti e le decisioni progettuali attuali.

## Flusso architetturale

```text
Controller
-> MusicPlaylistManagerFacade
-> CommandFactory
-> UndoManager
-> Command
-> TrackService / PlaylistService
-> Repository
-> SQLite
```

Il controller non conosce i command concreti e non dipende dalla persistenza.

## Verifica

Comando eseguito:

```bash
mvn clean test
```

Risultato:

```text
Tests run: 82
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Decisioni e limiti noti

- La cronologia Undo e mantenuta solamente in memoria.
- La cronologia viene persa alla chiusura dell'applicazione.
- Non e stato implementato il Redo.
- L'undo non ripristina stato, coda o tempo del playback.
- Le operazioni di ripristino composte da piu scritture non sono ancora
  racchiuse in una transazione applicativa unica.
- Alcuni metodi dei fake repository sono stub marcati `TODO` e dovranno essere
  completati quando verranno aggiunti test dedicati ai nuovi command.
- Questa PR introduce il supporto applicativo a `canUndo()` e
  `undoLastAction()`; l'eventuale pulsante Undo e il relativo refresh visuale
  devono essere verificati o integrati nel lavoro UI dello Sprint 3.
- Le funzionalita **US-27** (tag visuali) e **US-28** (playlist automatiche)
  non fanno parte di questa PR.

## Checklist

- [x] Task T3-01 / T3-09 completate
- [x] Command pattern introdotto nel layer applicativo
- [x] Undo per creazione ed eliminazione delle tracce
- [x] Undo per creazione ed eliminazione delle playlist
- [x] Undo per aggiunta e rimozione di tracce dalle playlist
- [x] Ordine delle tracce preservato durante il ripristino
- [x] Facade e bootstrap aggiornati
- [x] Architettura e linee guida aggiornate
- [x] Codice compilato correttamente
- [x] Suite Maven completata con 82 test superati
- [ ] Verifica finale dell'integrazione e del refresh UI da parte del team
- [ ] Code review
- [ ] Aggiornamento Trello e ore consuntivate
