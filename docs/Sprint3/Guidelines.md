# Sprint 3 Guidelines - Command e Undo

## 1. Scopo

Questo documento descrive l'evoluzione architetturale introdotta nello Sprint 3 per supportare l'annullamento delle operazioni su tracce e playlist.

La struttura precedente non e stata sostituita: MVC, Facade, Service e Repository restano validi. E stato aggiunto il **Command pattern** nel layer applicativo per rappresentare le operazioni modificative e conservare le informazioni necessarie al loro annullamento.

Il flusso principale delle mutazioni e ora:

```text
Controller
-> MusicPlaylistManagerFacade
-> CommandFactory
-> UndoManager
-> Command
-> Service
-> Repository
-> SQLite
```

Le letture, il playback e le operazioni che non partecipano all'undo continuano invece a passare direttamente dal facade ai service.

---

## 2. Cosa e cambiato

Nel package `application` e stato aggiunto:

```text
application.command
|-- Command
|-- CommandFactory
|-- UndoManager
`-- concreteCommands
    |-- AddTrackCommand
    |-- DeleteTrackCommand
    |-- CreatePlaylistCommand
    |-- DeletePlaylistCommand
    |-- AddTrackToPlaylistCommand
    `-- RemoveTrackFromPlaylistCommand
```

Sono inoltre cambiati:

- `MusicPlaylistManagerFacade`, che crea ed esegue i command per le mutazioni annullabili;
- `SqliteAppFactory`, che costruisce e condivide `CommandFactory` e `UndoManager`;
- `PlaylistService` e `PlaylistRepository`, che espongono le operazioni necessarie a ripristinare associazioni e posizioni;
- le implementazioni SQLite, che permettono di conoscere e ripristinare la posizione di una traccia in una playlist.

I controller non devono conoscere i command. Continuano a dipendere esclusivamente dal facade.

---

## 3. Ruolo dei componenti

### `Command`

E l'interfaccia comune delle operazioni annullabili:

```java
void execute();
void undo();
```

`execute()` applica la modifica. `undo()` ripristina lo stato precedente usando le informazioni salvate dall'istanza concreta.

Un command non deve:

- accedere direttamente a SQLite;
- creare repository concreti;
- contenere logica JavaFX;
- aggiornare direttamente la view;
- gestire l'intera cronologia degli undo.

### `CommandFactory`

Centralizza la creazione dei command concreti.

Riceve gli stessi `TrackService` e `PlaylistService` usati dal facade e li inserisce nei command. In questo modo:

- il facade non conosce i dettagli dei costruttori concreti;
- i command dipendono dai service, non dalle implementazioni SQLite;
- la creazione dei command resta uniforme.

Quando viene aggiunto un nuovo command, la factory deve esporre un metodo di creazione dedicato.

### `UndoManager`

Gestisce la cronologia dei command eseguiti con successo.

La cronologia e una pila LIFO:

```text
ultimo command eseguito -> primo command annullato
```

Il comportamento previsto e:

1. ricevere un command;
2. chiamare `execute()`;
3. inserire il command nella pila solo se l'esecuzione termina con successo;
4. con `undoLast()` selezionare il command piu recente;
5. chiamare `undo()`;
6. rimuoverlo dalla pila solo dopo un undo riuscito.

La cronologia:

- e condivisa da tutte le mutazioni eseguite tramite il facade;
- vive solo in memoria;
- viene persa alla chiusura dell'applicazione;
- supporta l'undo, ma attualmente non il redo.

### Command concreti

Ogni command rappresenta una sola operazione applicativa e conserva esclusivamente lo stato necessario per invertirla.

| Command | `execute()` | Stato conservato | `undo()` |
|---|---|---|---|
| `AddTrackCommand` | crea una traccia | traccia creata e relativo ID | elimina la traccia creata |
| `DeleteTrackCommand` | elimina una traccia | traccia eliminata e posizione in ogni playlist | ricrea la traccia e ripristina le associazioni |
| `CreatePlaylistCommand` | crea una playlist | playlist creata e relativo ID | elimina la playlist creata |
| `DeletePlaylistCommand` | elimina una playlist | playlist eliminata e lista ordinata delle tracce | ricrea la playlist e la ripopola nello stesso ordine |
| `AddTrackToPlaylistCommand` | aggiunge una traccia a una playlist | identificativi e avvenuta esecuzione | rimuove l'associazione creata |
| `RemoveTrackFromPlaylistCommand` | rimuove una traccia da una playlist | posizione originale della traccia | ripristina l'associazione nella posizione originale |

Gli snapshot devono essere raccolti prima dell'eliminazione. Dopo la cancellazione il repository potrebbe non avere piu i dati necessari per ricostruire lo stato precedente.

---

## 4. Ruolo del facade

`MusicPlaylistManagerFacade` resta l'unico punto di ingresso usato dai controller.

Per le mutazioni annullabili il facade:

1. chiede a `CommandFactory` il command corretto;
2. passa il command a `UndoManager`;
3. lascia che il command chiami i service;
4. coordina eventuali conseguenze sul playback.

Le operazioni instradate tramite command sono:

- aggiunta di una traccia;
- eliminazione di una traccia;
- creazione di una playlist;
- eliminazione di una playlist;
- aggiunta di una traccia a una playlist;
- rimozione di una traccia da una playlist.

Il facade espone inoltre:

- `undoLastAction()`, per annullare l'ultima mutazione riuscita;
- `canUndo()`, per sapere se esiste un command annullabile.

Restano chiamate dirette ai service:

- lettura e ricerca di tracce e playlist;
- aggiornamento dei metadati di una traccia;
- operazioni di playback;
- lettura dello stato corrente.

Questa distinzione evita di creare command per operazioni che non modificano lo stato o che non sono ancora comprese nei requisiti dell'undo.

---

## 5. Playback e undo

L'eliminazione di una traccia o di una playlist puo rendere non valido il playback corrente. Per questo il facade continua a coordinare `PlaybackService` dopo l'esecuzione del command di eliminazione.

La scelta attuale e:

- l'eliminazione aggiorna o interrompe il playback quando necessario;
- l'undo ripristina tracce, playlist, associazioni e ordine;
- l'undo non riprende il playback precedente e non ripristina tempo, stato o coda.

Questa scelta mantiene separati:

- lo stato persistente di catalogo e playlist;
- lo stato temporaneo della sessione di playback.

Un eventuale ripristino del playback richiederebbe uno snapshot dedicato e deve essere trattato come una nuova decisione progettuale, non aggiunto implicitamente nei command esistenti.

---

## 6. Supporto di service e repository

I command non devono aggirare il layer applicativo. Se per un undo serve una nuova operazione, questa deve essere esposta dal service appropriato e delegata al repository tramite la sua interfaccia.

Per conservare l'ordine delle playlist sono necessarie operazioni equivalenti a:

- ottenere la posizione di una traccia in una playlist;
- aggiungere una traccia in una posizione specifica;
- verificare l'esistenza di un'associazione;
- ottenere le tracce di una playlist nell'ordine persistito;
- ricreare un'entita mantenendo il suo ID quando viene eseguito l'undo.

Il percorso corretto resta:

```text
Command
-> PlaylistService o TrackService
-> PlaylistRepository o TrackRepository
-> implementazione SQLite
```

Non va introdotto nel command un riferimento a `SqlitePlaylistRepository`, `SqliteTrackRepository` o `DatabaseConnectionManager`.

---

## 7. Cablaggio nel bootstrap

`SqliteAppFactory` costruisce un unico grafo di oggetti condiviso:

```text
Repository SQLite
-> Service
-> CommandFactory
-> UndoManager
-> MusicPlaylistManagerFacade
-> ControllerFactory
-> Controller
```

`CommandFactory` deve ricevere le stesse istanze di `TrackService` e `PlaylistService` usate dal facade.

Deve esistere un solo `UndoManager` per la sessione applicativa. Crearne uno nuovo a ogni operazione separerebbe le cronologie e renderebbe impossibile annullare correttamente l'ultima azione globale.

---

## 8. Come aggiungere un nuovo command

Per aggiungere una nuova operazione annullabile:

1. definire con precisione lo stato precedente da ripristinare;
2. creare una classe in `application.command.concreteCommands`;
3. implementare `Command`;
4. iniettare solo i service necessari;
5. salvare durante `execute()` i dati richiesti da `undo()`;
6. aggiungere il metodo di creazione a `CommandFactory`;
7. cablare l'operazione nel facade tramite `UndoManager.executeAndPush(...)`;
8. lasciare controller e view indipendenti dal command concreto;
9. aggiungere test per esecuzione, undo, ordine e casi di errore.

Se il command richiede dati non disponibili, bisogna estendere prima il contratto del service e del repository. Non si deve risolvere il problema interrogando direttamente SQLite dal command.

---

## 9. Regole per implementazioni future

- Salvare lo snapshot prima di modificare o eliminare i dati.
- Conservare gli ID originali quando l'undo deve ricreare la stessa entita.
- Conservare esplicitamente le posizioni quando l'ordine ha significato.
- Usare copie delle collezioni da ripristinare, evitando riferimenti modificabili condivisi.
- Inserire nella cronologia solo command eseguiti con successo.
- Rimuovere un command dalla cronologia solo dopo un undo riuscito.
- Evitare che `undo()` richiami il facade: deve usare direttamente i service iniettati.
- Non inserire regole di business nei controller.
- Non trasformare operazioni di sola lettura in command.
- Non modificare il playback durante l'undo senza un requisito esplicito.

---

## 10. Limiti attuali e lavoro futuro

L'implementazione corrente ha questi limiti intenzionali:

- nessuna persistenza della cronologia tra sessioni;
- nessun redo;
- nessun ripristino del playback;
- nessun raggruppamento di piu command in una singola transazione applicativa;
- i ripristini composti da piu scritture possono richiedere in futuro un supporto transazionale esplicito;
- alcuni fake repository nei test contengono metodi `TODO` aggiunti soltanto per mantenere la compilazione e devono essere completati dai responsabili dei test.

Prima di estendere il meccanismo e necessario verificare che il nuovo comportamento rispetti i requisiti dello Sprint, la separazione dei layer e la Definition of Done.

---

## 11. Checklist rapida

Una modifica al sistema Command e coerente quando:

- il controller chiama solo il facade;
- il facade crea il command tramite `CommandFactory`;
- `UndoManager` gestisce esecuzione e cronologia;
- il command usa i service e non SQLite;
- `execute()` salva lo stato necessario;
- `undo()` ripristina dati, associazioni e ordine;
- gli errori non lasciano nella pila command non eseguiti;
- il playback segue la decisione documentata;
- i test compilano e `mvn clean test` passa.
