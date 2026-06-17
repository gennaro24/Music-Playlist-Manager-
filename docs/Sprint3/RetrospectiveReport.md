# Sprint 3 Review and Retrospective Report

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design 2025/2026  
**Group:** n. 14 - AH  
**Sprint:** Sprint 3  

---

## 1. Sprint outcome

Durante lo Sprint 3 il team ha consegnato l'incremento finale del Music Playlist Manager, concentrandosi su tre obiettivi principali:

1. introdurre l'undo globale sulle operazioni mutative principali di catalogo e playlist;
2. aggiungere la gestione dei tag visuali sulle tracce;
3. creare playlist automatiche basate su criteri semplici come genere, anno e tag.

Lo Sprint ha inoltre incluso una fase finale di refactoring e stabilizzazione, necessaria per chiudere il progetto con una struttura piu pulita e coerente con i principi SOLID e YAGNI. In particolare, il team ha separato meglio le responsabilita dei controller, ha corretto bug legati alla persistenza SQLite e ha ridotto codice inutilizzato o non piu coerente con l'architettura finale.

### Sprint metrics

| Elemento | Valore |
|---|---:|
| Story Points pianificati | 71 SP |
| Story Points completati | 71 SP |
| User Stories pianificate | 8 |
| User Stories completate | 8 |
| Test automatici eseguiti | 184 |
| Esito test Maven | 0 failure, 0 error |

### User Stories completate

| ID | User Story | Story Points | Status |
|---|---|---:|---|
| US-19 | Undo creazione globale canzone | 8 | DONE |
| US-20 | Undo eliminazione globale canzone | 13 | DONE |
| US-21 | Undo creazione globale playlist | 8 | DONE |
| US-22 | Undo eliminazione globale playlist | 13 | DONE |
| US-23 | Undo aggiunta traccia alla playlist | 8 | DONE |
| US-24 | Undo rimozione traccia dalla playlist | 13 | DONE |
| US-27 | Aggiungere tag visuali alle tracce | 3 | DONE |
| US-28 | Creare automaticamente playlist per genere, anno o tag | 5 | DONE |
| **Total** | **Sprint 3 Scope** | **71** | **DONE** |

### Motivazione dei 71 Story Points

I 71 Story Points dello Sprint 3 sono superiori alla velocity osservata negli Sprint 1 e 2. Questo dato non va letto come un improvviso aumento lineare della capacita produttiva, ma come effetto diretto del lavoro architetturale svolto negli Sprint precedenti.

In Sprint 1 il team ha costruito la prima thin slice end-to-end: UI, facade, service, repository e SQLite. In Sprint 2 ha rifattorizzato il bootstrap applicativo, consolidato la facade, introdotto factory, repository interface e un sottosistema di playback piu ordinato. Di conseguenza, in Sprint 3 molte feature hanno potuto appoggiarsi a una struttura gia pronta.

Il Command pattern e la gestione dei tag non hanno richiesto di stravolgere il layer applicativo o la business logic gia esistente. Il team ha principalmente aggiunto un nuovo livello applicativo per rappresentare operazioni annullabili, esteso la facade come punto di ingresso stabile e introdotto repository/service dedicati per i tag. Questo ha reso possibile completare uno scope ampio senza riscrivere l'architettura di base.

---

## 2. Delivered increment

L'incremento consegnato in Sprint 3 rende l'applicazione piu completa dal punto di vista dell'esperienza utente e piu robusta dal punto di vista interno.

Dal punto di vista funzionale, l'applicazione ora consente di:

- annullare la creazione di una traccia nel catalogo;
- annullare l'eliminazione globale di una traccia, ripristinandola anche nelle playlist in cui era presente;
- annullare la creazione di una playlist;
- annullare l'eliminazione di una playlist, recuperando le tracce contenute;
- annullare l'aggiunta di una traccia a una playlist;
- annullare la rimozione di una traccia da una playlist, mantenendo l'ordine originale;
- creare, assegnare, rimuovere e visualizzare tag associati alle tracce;
- creare playlist automatiche filtrando il catalogo per genere, anno e tag;
- usare l'anteprima delle playlist automatiche prima della creazione effettiva;
- annullare anche la creazione di playlist automatiche tramite la cronologia globale.

L'undo e' gestito come cronologia in memoria della sessione applicativa. Il sistema supporta l'annullamento dell'ultima operazione mutativa, ma non introduce il redo perche non richiesto dal backlog e non necessario per la release finale.

---

## 3. Sprint flow and commit trace

Il flusso dello Sprint 3 e' stato incrementale.

La prima parte dello Sprint ha introdotto il backend dell'undo tramite Command pattern. I commit principali in questa fase includono:

- `ef3e02f` - introduzione dell'undo command-based per tracce e playlist;
- `c0bbe85` - test JUnit per `UndoManager` e Command pattern;
- `2c92b93` - correzione atomica dell'undo su eliminazione traccia tramite transazione SQL;
- `4adde51` - miglioramento dei test su `DeleteTrackCommand` per gestire retry in caso di restore fallito.

La seconda parte ha integrato l'undo con la GUI e ha introdotto il sistema di tag:

- `63d82bd` - abilitazione dei vincoli foreign key in SQLite;
- `66ec3e7` - integrazione undo nella GUI e fix su undo della cancellazione traccia;
- `788715b` - implementazione del tagging system;
- `a046cae` - test per `TagService` e `SqliteTagRepository`;
- `0a527d1` - aggiornamento di `TrackController` e vista tracce per la gestione tag;
- `1c10143` - gestione dei tag anche nel `DeleteTrackCommand`;
- `2cb58df` - refactoring della gestione tag nel controller.

La parte finale dello Sprint ha implementato le playlist automatiche e ha stabilizzato l'integrazione:

- `00f6734` - introduzione dello scheletro di `AutoPlaylistService` e `AutoPlaylistCriteria`;
- `49feebe` - anteprima end-to-end delle playlist automatiche;
- `563fa44` - creazione playlist automatica da GUI;
- `187f766` - fix duplicati e undo per nuove playlist automatiche;
- `4ef2e6a` - test JUnit per `AutoPlaylistService`;
- `9860631`, `f803938`, `e005a00` - refactoring finale, rimozione warning, prevenzione bug logici e creazione playlist con tracce resa atomica;
- `95ef713` - fix sul playback sequenziale quando viene eliminata una traccia futura dalla coda;
- `28d39c5`, `79a9e89` - miglioramenti finali su undo, selezione playlist e interazioni UI.

Questo andamento conferma che lo Sprint e' partito dalla base applicativa, ha poi integrato la UI e ha chiuso con bug fixing, test e refactoring.

---

## 4. Architectural evolution

Lo Sprint 3 non sostituisce l'architettura costruita negli Sprint 1 e 2. La estende.

Il flusso architetturale principale rimane:

```text
FXML/View
-> Controller
-> MusicPlaylistManagerFacade
-> Service
-> Domain / Repository Interface
-> SQLite Repository
-> Database
```

Per le operazioni annullabili viene aggiunto un passaggio applicativo:

```text
Controller
-> MusicPlaylistManagerFacade
-> CommandFactory
-> UndoManager
-> Command
-> Service
-> Repository
```

Le letture, il playback e le operazioni non annullabili continuano a passare direttamente dalla facade ai service. Questa scelta mantiene l'architettura semplice: i Command vengono usati dove esiste un requisito reale di undo, evitando di trasformare ogni chiamata applicativa in un command solo per uniformita.

### Command e UndoManager

Il Command pattern viene applicato alle operazioni mutative principali:

- `AddTrackCommand`;
- `DeleteTrackCommand`;
- `CreatePlaylistCommand`;
- `DeletePlaylistCommand`;
- `AddTrackToPlaylistCommand`;
- `RemoveTrackFromPlaylistCommand`;
- `CreateAutoPlaylistCommand`.

Ogni command incapsula l'operazione e conserva solo lo stato necessario per annullarla. `UndoManager` mantiene una pila LIFO di command eseguiti con successo e annulla l'ultima operazione disponibile.

Questa soluzione e' coerente con Single Responsibility e Open/Closed Principle: la facade resta il punto di ingresso per la UI, mentre la logica di undo e' isolata nei command concreti.

### Tag e playlist automatiche

Per i tag sono stati introdotti:

- `Tag`;
- `TagRepository`;
- `SqliteTagRepository`;
- `TagService`;
- metodi dedicati nella facade;
- aggiornamenti UI in `TrackController`.

Per le playlist automatiche sono stati introdotti:

- `AutoPlaylistCriteria`;
- `AutoPlaylistService`;
- `AutoPlaylistDialog`;
- integrazione nella facade e nella GUI.

I criteri di creazione automatica vengono applicati in AND. Se sono presenti genere, anno e tag, una traccia deve soddisfare tutti i criteri per essere inclusa.

---

## 5. Design patterns and design principles

### Facade

`MusicPlaylistManagerFacade` resta il punto di accesso stabile per i controller. In Sprint 3 viene estesa per esporre undo, tag e playlist automatiche senza far conoscere alla UI i dettagli di service, repository o command concreti.

### Command

Il Command pattern rappresenta il principale pattern introdotto nello Sprint 3. Ogni operazione annullabile viene modellata come oggetto con `execute()` e `undo()`.

Il pattern e' stato usato perche il requisito dell'undo non riguarda solo un singolo caso d'uso, ma un insieme di mutazioni diverse su catalogo, playlist e associazioni. Centralizzare la cronologia in `UndoManager` evita duplicazione nei controller e mantiene tracciabile l'ultima azione globale.

### Factory

`CommandFactory` crea i command concreti con i service necessari. Questo evita che la facade conosca i costruttori e le dipendenze specifiche di ogni command.

La factory introdotta in Sprint 2 per il bootstrap resta valida: `SqliteAppFactory` costruisce repository, service, facade, `CommandFactory`, `UndoManager` e `AutoPlaylistService`.

### Repository

Il pattern Repository viene esteso ai tag tramite `TagRepository` e `SqliteTagRepository`. La logica applicativa continua a dipendere da interfacce, non da dettagli SQLite.

### Stream API e criteri compositi

Durante lo Sprint il team ha valutato l'uso di pattern come Composite o Observer per alcune parti della UI e per la gestione dei criteri. La soluzione finale non implementa un Composite pattern formale.

Per le playlist automatiche il codice usa la Stream API di Java: il servizio parte dall'intero catalogo e applica in sequenza `filter` su genere, anno e tag. Dal punto di vista concettuale questa e' una composizione funzionale di criteri, simile nell'effetto a una combinazione di filtri, ma non va descritta come Composite pattern vero e proprio perche non esistono oggetti criterio organizzati in una gerarchia comune.

Anche Observer non e' stato introdotto come pattern esplicito per la view. Il team ha preferito aggiornamenti controllati tramite controller, callback e refresh mirati, evitando un'infrastruttura di osservazione piu ampia non richiesta dai requisiti finali.

### SOLID e YAGNI

Il refactoring finale ha avuto l'obiettivo di rispettare meglio SOLID e YAGNI:

- separazione delle responsabilita nei controller, soprattutto intorno a `TrackController`;
- riduzione di codice inutilizzato o non piu coerente;
- correzione di bug di persistenza e vincoli SQLite;
- gestione piu sicura delle operazioni atomiche;
- mantenimento del redo fuori scope, perche non richiesto;
- uso dei command solo per operazioni effettivamente annullabili.

---

## 6. Refactoring and stabilization

La fase finale dello Sprint e' stata dedicata alla stabilizzazione.

Le attivita principali sono state:

- abilitazione dei vincoli di chiave esterna nel database SQLite;
- introduzione delle tabelle `tags` e `track_tags`;
- fix dell'undo su eliminazione traccia, includendo ripristino delle associazioni;
- gestione dei tag quando una traccia viene eliminata e poi ripristinata;
- prevenzione di playlist automatiche duplicate;
- creazione atomica delle playlist automatiche con tracce;
- aggiornamento del playback quando viene eliminata una traccia presente nella coda;
- pulizia di classi inutilizzate e warning;
- maggiore coerenza tra controller, facade e service.

Questa fase e' stata importante perche lo Sprint 3 ha introdotto feature trasversali. Undo, tag e playlist automatiche toccano piu layer: UI, facade, service, repository e database. Il refactoring finale ha ridotto il rischio di lasciare logica duplicata o comportamenti incoerenti tra i casi d'uso.

---

## 7. Testing and quality

La suite Maven finale contiene **184 test superati**, con **0 failure** e **0 error**.

Le aree principali coperte dai test includono:

- `UndoManager`;
- `CommandFactory`;
- command concreti per tracce e playlist;
- integrazione undo esposta dalla facade;
- `TagService`;
- `SqliteTagRepository`;
- `AutoPlaylistService`;
- `PlaylistService`;
- `PlaybackService`;
- model e strategie di playback;
- test mirato su `TrackController`.

Rispetto allo Sprint 2, il numero di test e' aumentato sensibilmente per coprire la nuova complessita introdotta dal Command pattern e dai tag. I test sui command sono particolarmente importanti perche verificano sia l'esecuzione sia il ripristino dello stato precedente.

Comando eseguito:

```text
mvn clean test
```

Esito:

```text
Tests run: 184, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. Product Backlog impact

Lo Sprint 3 chiude le principali User Stories rimaste nel Product Backlog per la release finale.

Le story di undo aumentano la sicurezza d'uso dell'applicazione: l'utente puo correggere errori comuni senza ricostruire manualmente catalogo e playlist. Le story sui tag aggiungono un modo piu ricco per classificare le tracce. Le playlist automatiche trasformano questi dati in valore funzionale, permettendo di creare raccolte senza selezionare manualmente ogni brano.

La scelta di non implementare redo, Composite formale o Observer formale e' coerente con YAGNI: erano soluzioni possibili, ma non necessarie per soddisfare gli Acceptance Criteria dello Sprint 3.

---

## 9. Git and integration process

Il team ha continuato a lavorare con branch dedicati e Pull Request verso `main`.

Durante lo Sprint 3 i branch principali hanno riguardato:

- backend undo e command;
- tag sulle tracce;
- playlist automatiche;
- fix e refactoring finali.

Il processo di integrazione e' stato supportato da:

- commit incrementali e descrittivi;
- branch separati per feature e fix;
- test Maven prima della chiusura;
- review sulle aree piu delicate, in particolare command, persistenza e controller;
- merge progressivi verso `main`.

La gestione pulita di branch e commit ha aiutato a ricostruire il flusso dello Sprint e a isolare rapidamente i bug emersi durante l'integrazione.

---

## 10. Sprint Retrospective

### Start

- Introdurre una breve checklist di impatto quando una feature tocca UI, facade, service, repository e database.
- Registrare subito nel backlog tecnico i refactoring emersi durante lo sviluppo, senza aspettare la chiusura dello Sprint.
- Preparare prima gli scenari manuali di demo per le feature trasversali come undo e playlist automatiche.

### More of

- Maggiore comunicazione tra i membri del team quando una modifica coinvolge piu layer.
- Maggiore intraprendenza nella risoluzione autonoma dei problemi, soprattutto su bug di integrazione.
- Condivisione rapida delle decisioni tecniche prese durante lo sviluppo.
- Collaborazione piu frequente sui punti delicati, come undo, persistenza e aggiornamento della UI.

### Keep doing

- Continuare con code review pulite e focalizzate.
- Mantenere branch e commit ordinati, separando feature, fix, test e refactoring.
- Eseguire `mvn clean test` prima delle integrazioni principali.
- Usare facade, service e repository interface come confini architetturali stabili.
- Applicare pattern solo quando risolvono un problema reale del backlog.

### Less of

- Ridurre l'accumulo di piccoli fix alla fine dello Sprint.
- Ridurre commenti temporanei o messaggi di commit poco descrittivi.
- Ridurre modifiche contemporanee allo stesso controller senza una breve sincronizzazione.
- Ridurre la documentazione aggiornata solo dopo la chiusura delle feature.

### Stop

- Smettere di lasciare comunicazione insufficiente quando una modifica ha impatto su piu membri del team.
- Smettere di rimandare la segnalazione di bug di integrazione scoperti durante lo sviluppo.
- Smettere di introdurre codice non necessario solo per anticipare requisiti non presenti.

---

## 11. Final assessment

Lo Sprint 3 puo essere considerato completato con successo.

Il team ha consegnato tutte le User Stories pianificate, per un totale di 71 Story Points, e ha chiuso la release finale con una suite automatizzata di 184 test superati. L'incremento aggiunge funzionalita importanti per l'utente finale, come undo, tag e playlist automatiche, mantenendo al tempo stesso una struttura coerente con l'architettura gia consolidata negli Sprint precedenti.

Il risultato piu rilevante non e' solo la quantita di feature completate, ma il fatto che siano state integrate senza stravolgere il progetto. Gli Sprint 1 e 2 avevano preparato una base solida; Sprint 3 l'ha sfruttata per aggiungere valore funzionale, completare la qualita interna e rendere il prodotto pronto per la demo finale.
