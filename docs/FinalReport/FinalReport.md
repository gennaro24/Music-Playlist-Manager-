# Final Project Report - Music Playlist Manager

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design 2025/2026  
**Group:** n. 14 - AH  
**Release:** Final release after Sprint 3  

---

## 1. Executive summary

Il Music Playlist Manager e' evoluto in tre Sprint da una prima applicazione JavaFX funzionante a un prodotto finale con catalogo musicale, playlist, playback simulato, undo globale, tag visuali e playlist automatiche.

L'evoluzione del progetto non e' stata solo funzionale. Ogni Sprint ha lasciato una base tecnica piu solida per il successivo:

- Sprint 1 ha costruito la prima thin slice end-to-end, collegando UI, facade, service, domain model, repository e SQLite;
- Sprint 2 ha consolidato l'architettura, separando la composizione delle dipendenze dai controller e strutturando il playback con stato, coda, snapshot e strategy;
- Sprint 3 ha sfruttato quella base per introdurre feature piu trasversali, come Command/Undo, tag e playlist automatiche, chiudendo con refactoring finale e stabilizzazione.

Il risultato finale e' un'applicazione dimostrabile, testata e organizzata secondo una layered architecture con MVC nel Presentation Layer, Facade come punto di accesso applicativo, Service per i casi d'uso, Repository per la persistenza e pattern mirati dove i requisiti lo richiedevano.

---

## 2. Progressive metrics

| Sprint | Scope pianificato | Scope completato | SP cumulativi | Test automatici | Focus principale |
|---|---:|---:|---:|---:|---|
| Sprint 1 | 30 SP | circa 30 SP | 30 SP | n.d. | Thin slice end-to-end |
| Sprint 2 | 43 SP | 43 SP | 73 SP | 82 | Refactoring architetturale e playback |
| Sprint 3 | 71 SP | 71 SP | 144 SP | 184 | Undo, tag, playlist automatiche e stabilizzazione |

Il totale progressivo ufficiale arriva a **144 Story Points completati** considerando gli scope pianificati dei tre Sprint. La US-18, emersa e completata durante Sprint 1, non e' stata conteggiata nella velocity ufficiale dello Sprint per mantenere stabile la metrica rispetto allo Sprint Backlog iniziale.

L'aumento da 30 SP a 43 SP e poi a 71 SP non va interpretato come un aumento lineare della capacita del team. E' soprattutto l'effetto della riduzione progressiva del costo di integrazione. A ogni Sprint il team ha rimosso colli di bottiglia architetturali e ha reso piu economico aggiungere nuove funzionalita sopra confini gia definiti.

---

## 3. Sprint 1 - Prima thin slice end-to-end

Lo Sprint 1 ha avuto l'obiettivo di costruire una prima versione funzionante del prodotto. Il team ha implementato le funzionalita di base:

- aggiunta e visualizzazione delle tracce nel catalogo;
- creazione e visualizzazione delle playlist;
- visualizzazione del contenuto di una playlist;
- aggiunta e rimozione di tracce dalle playlist;
- avvio di una prima simulazione di playback;
- pausa logica della traccia corrente, anche se senza timer completo;
- visualizzazione delle playlist create come lavoro emerso.

La scelta principale dello Sprint 1 e' stata consegnare una thin slice completa, cioe' un flusso che attraversasse tutta l'applicazione:

```text
JavaFX UI
-> Controller
-> MusicPlaylistManagerFacade
-> Service
-> Repository Interface
-> SQLite Repository
-> Database
```

Questa impostazione ha permesso al team di validare presto l'architettura reale, non solo un modello teorico. Gia' nello Sprint 1 erano presenti separazioni concettuali utili: domain model, service applicativi, repository e facade.

### Impatto sul burndown

Il burndown dello Sprint 1 ha avuto una parte iniziale quasi piatta. Questo non indicava inattivita, ma lavoro tecnico propedeutico: setup Maven/JavaFX, configurazione SQLite, `DatabaseConnectionManager`, `DatabaseInitializer`, repository concrete e collegamento con facade e service.

La discesa piu ripida e' arrivata dopo questa fase perche, una volta stabilizzata l'infrastruttura minima, molte User Stories funzionali hanno potuto essere chiuse rapidamente sopra lo stesso flusso end-to-end.

### Debito tecnico emerso

Il principale debito tecnico individuato riguardava l'accoppiamento tra controller JavaFX e creazione delle dipendenze infrastrutturali. In Sprint 1 questa scelta ha accelerato l'integrazione iniziale, ma avrebbe reso piu difficile testare, mantenere ed estendere l'applicazione.

Questo debito e' diventato input diretto per Sprint 2.

---

## 4. Sprint 2 - Consolidamento architetturale e playback

Lo Sprint 2 ha trasformato l'applicazione da prototipo end-to-end a sistema piu mantenibile. Gli obiettivi principali erano:

1. rifattorizzare il ciclo di vita delle istanze e disaccoppiare la UI dall'infrastruttura;
2. completare il playback simulato con timer, pausa, sequenziale, skip, shuffle, loop e snapshot di stato.

Le User Stories completate hanno incluso modifica ed eliminazione tracce, eliminazione playlist, completamento della pausa, playback di catalogo/playlist, skip, shuffle, loop e visualizzazione dello stato corrente.

### Refactoring architetturale

Il refactoring di Sprint 2 ha introdotto:

- `AppFactory` e `SqliteAppFactory` come composition root;
- `ControllerFactory` per integrare JavaFX/FXML con constructor injection;
- controller dipendenti dalla sola `MusicPlaylistManagerFacade`;
- service dipendenti da repository interface;
- fake repository e fake facade nei test.

Il flusso runtime e' diventato piu pulito:

```text
Main
-> SqliteAppFactory
-> Repository concrete
-> Service
-> MusicPlaylistManagerFacade
-> ControllerFactory
-> JavaFX Controllers
-> FXML/View
```

Questo ha ridotto il costo delle feature successive. I controller non dovevano piu conoscere SQLite o costruire service; la facade diventava il confine stabile tra Presentation Layer e Application Layer.

### Playback e Strategy

Il playback e' stato modellato con:

- `PlaybackQueue`;
- `PlaybackSnapshot`;
- `PlaybackSource`;
- `PlaybackMode`;
- `PlaybackStrategy`;
- strategie concrete per sequenziale, shuffle e repeat all.

Il pattern Strategy ha separato gli algoritmi di avanzamento della coda dal coordinamento applicativo di `PlaybackService`. Questo ha reso piu semplice aggiungere o correggere modalita di riproduzione senza concentrare tutta la logica in un unico metodo.

### Impatto sul burndown

Anche in Sprint 2 la discesa iniziale del burndown e' stata condizionata da lavoro tecnico. Il refactoring della composizione delle dipendenze era bloccante: prima di aggiungere nuove feature di playback e gestione playlist, il team doveva rendere piu stabile il modo in cui UI, facade, service e repository comunicavano.

Dopo il refactoring, la curva e' scesa piu rapidamente per due motivi:

- le feature di playback condividevano gli stessi oggetti di stato (`PlaybackQueue`, `PlaybackSnapshot`, `PlaybackMode`);
- le modifiche alla UI potevano passare dalla facade senza toccare la persistenza o il bootstrap.

La discesa ripida e' quindi stata una conseguenza del fatto che piu User Stories erano costruite sopra lo stesso sottosistema gia stabilizzato.

---

## 5. Sprint 3 - Undo, tag, playlist automatiche e release finale

Lo Sprint 3 ha chiuso la release finale con uno scope di **71 SP**, superiore agli Sprint precedenti. Le User Stories completate sono state:

- US-19: undo creazione globale canzone;
- US-20: undo eliminazione globale canzone;
- US-21: undo creazione globale playlist;
- US-22: undo eliminazione globale playlist;
- US-23: undo aggiunta traccia alla playlist;
- US-24: undo rimozione traccia dalla playlist;
- US-27: tag visuali sulle tracce;
- US-28: playlist automatiche per genere, anno o tag.

Questo Sprint ha avuto una velocity apparente molto alta perche gli Sprint 1 e 2 avevano gia preparato la struttura necessaria. Il team non ha dovuto riscrivere il layer applicativo o la business logic: ha aggiunto nuovi componenti coerenti con i confini esistenti.

### Command e undo globale

Per implementare l'undo, il team ha introdotto il Command pattern:

```text
Controller
-> MusicPlaylistManagerFacade
-> CommandFactory
-> UndoManager
-> Command
-> Service
-> Repository
```

I command concreti incapsulano le operazioni annullabili e conservano lo stato minimo necessario per ripristinare la situazione precedente:

- traccia creata o eliminata;
- playlist creata o eliminata;
- associazioni tra playlist e tracce;
- posizione originale delle tracce;
- contenuto delle playlist automatiche create.

`UndoManager` mantiene una cronologia LIFO in memoria e annulla l'ultima operazione riuscita. La scelta e' coerente con YAGNI: e' stato implementato undo, ma non redo, perche il redo non era richiesto dagli Acceptance Criteria.

### Tag e repository dedicato

La gestione dei tag ha esteso il modello con:

- `Tag`;
- `TagRepository`;
- `SqliteTagRepository`;
- `TagService`;
- tabelle `tags` e `track_tags`;
- integrazione nel `TrackController` e nella facade.

Il pattern Repository ha permesso di introdurre la persistenza dei tag senza esporre dettagli SQLite al resto dell'applicazione.

### Playlist automatiche e Stream API

Le playlist automatiche sono state implementate tramite `AutoPlaylistCriteria` e `AutoPlaylistService`.

Il servizio parte dall'intero catalogo e applica in sequenza filtri per genere, anno e tag. I criteri sono combinati in AND: se sono presenti piu criteri, una traccia deve soddisfarli tutti.

Il team ha valutato pattern come Composite o Observer, ma la soluzione finale usa la Stream API di Java. Questa scelta e' corretta perche il requisito richiedeva una composizione semplice di filtri, non una gerarchia estendibile di oggetti criterio. Dal punto di vista concettuale i filtri sono componibili, ma non si tratta di un Composite pattern formale.

### Refactoring finale

La fase finale dello Sprint 3 ha incluso:

- abilitazione dei vincoli foreign key in SQLite;
- fix dell'undo su eliminazione traccia;
- gestione dei tag durante delete e restore;
- prevenzione di playlist automatiche duplicate;
- creazione atomica delle playlist automatiche con tracce;
- aggiornamento del playback quando viene eliminata una traccia nella coda;
- pulizia di warning, classi inutilizzate e bug logici;
- maggiore separazione delle responsabilita nei controller, soprattutto `TrackController`.

Questa fase ha rafforzato SOLID e YAGNI: responsabilita piu separate, meno codice non richiesto, pattern applicati solo dove portavano valore reale.

### Impatto sul burndown dello Sprint 3

La discesa ripida dello Sprint 3 e' la piu importante da spiegare.

Lo Sprint partiva con uno scope ampio, ma molte User Stories erano variazioni dello stesso meccanismo architetturale:

- US-19, US-20, US-21, US-22, US-23 e US-24 condividono `Command`, `CommandFactory` e `UndoManager`;
- US-27 introduce tag, poi US-28 riusa quei tag come criterio per playlist automatiche;
- le playlist automatiche riusano `TrackService`, `PlaylistService`, `TagService` e la facade gia esistente;
- la UI puo invocare nuove feature senza conoscere repository o database.

La prima parte dello Sprint ha avuto un investimento tecnico sul backend dell'undo. Dopo che il pattern Command e la cronologia erano stabili, molte story sono state chiuse rapidamente perche richiedevano soprattutto command concreti diversi, non architetture diverse.

In altre parole, la discesa finale non e' stata casuale: e' il risultato dell'effetto leva dei refactoring precedenti. La base architetturale ha trasformato feature apparentemente grandi in estensioni locali e testabili.

---

## 6. Evolution of the final architecture

L'architettura finale puo essere descritta come MVC + layered architecture:

```text
View / FXML
-> JavaFX Controller
-> MusicPlaylistManagerFacade
-> Application Service
-> Domain Model / Strategy / Command
-> Repository Interface
-> SQLite Repository
-> Database
```

La UI gestisce input e aggiornamento visuale. I controller non creano database, repository o service. La facade offre un'API applicativa unica. I service implementano i casi d'uso. Il dominio contiene model e regole. I repository isolano la persistenza.

Per le operazioni annullabili, il layer applicativo passa attraverso command e undo manager. Per il playback, usa strategie dedicate. Per la persistenza, usa repository interface e implementazioni SQLite.

Questa evoluzione ha reso il sistema:

- piu testabile, perche service e command possono essere verificati con fake repository;
- piu manutenibile, perche le responsabilita sono distribuite su componenti piccoli;
- piu estendibile, perche nuove feature possono appoggiarsi a facade, service e repository esistenti;
- piu robusto, perche bug trasversali sono stati isolati e corretti con test mirati.

---

## 7. Design patterns and technical value

### Facade

`MusicPlaylistManagerFacade` ha ridotto l'accoppiamento tra UI e logica applicativa. I controller chiamano un solo punto di accesso invece di conoscere service, repository o command concreti.

Vantaggio: feature nuove come undo, tag e playlist automatiche sono state esposte alla UI senza rompere il Presentation Layer.

### Repository

`TrackRepository`, `PlaylistRepository` e `TagRepository` isolano la persistenza. Le implementazioni SQLite restano nel layer infrastrutturale.

Vantaggio: i test possono usare repository fake/in-memory e i service non dipendono direttamente da SQL.

### Factory / Composition Root

`SqliteAppFactory` centralizza la costruzione delle dipendenze. `ControllerFactory` collega JavaFX alla dependency injection manuale.

Vantaggio: i controller sono piu semplici e non sono responsabili del bootstrap applicativo.

### Strategy

Le strategie di playback separano gli algoritmi di avanzamento della coda.

Vantaggio: sequenziale, shuffle e repeat all possono evolvere senza rendere `PlaybackService` un blocco monolitico.

### Command

I command modellano le mutazioni annullabili.

Vantaggio: l'undo e' uniforme, testabile e centralizzato. Ogni command conserva il proprio stato di ripristino senza duplicare logica nei controller.

### Stream API

La Stream API viene usata per comporre i filtri delle playlist automatiche.

Vantaggio: il codice resta semplice e leggibile. Il team ha evitato un Composite pattern formale perche sarebbe stato piu complesso del necessario rispetto ai requisiti.

---

## 8. Story points and refactoring impact

Il dato piu importante e' che i refactoring non hanno rallentato il progetto nel medio periodo. Hanno creato le condizioni per aumentare la capacita di consegna.

| Fase | Investimento tecnico | Effetto sullo Sprint successivo |
|---|---|---|
| Sprint 1 | Setup end-to-end, repository, facade, SQLite | Base reale per sviluppare feature senza ripartire da zero |
| Sprint 2 | Composition root, controller injection, playback state, strategy | Riduzione dell'accoppiamento e maggiore velocita su feature trasversali |
| Sprint 3 | Command/Undo, tag repository, refactoring finale, atomicita | Chiusura rapida di piu story simili e maggiore stabilita finale |

Lo Sprint 3 ha potuto completare 71 SP perche molte story avevano una struttura comune. Una volta implementato il nucleo dell'undo, i command concreti hanno seguito uno schema ripetibile. Una volta introdotti tag e criteri, le playlist automatiche hanno riusato servizi gia presenti.

Questo conferma il valore dei refactoring fatti in modo mirato: non refactoring estetici, ma interventi architetturali legati a problemi reali emersi dal backlog.

---

## 9. Burndown interpretation

Le discese ripide nei burndown dei tre Sprint hanno motivazioni diverse ma collegate.

### Sprint 1

La discesa e' arrivata dopo il setup tecnico iniziale. Finche mancavano database, repository e collegamento UI-service, poche story potevano essere chiuse. Una volta pronta la thin slice, diverse feature base sono state completate rapidamente.

### Sprint 2

La discesa e' arrivata dopo il refactoring del bootstrap e l'introduzione degli oggetti di playback. Molte story di playback condividevano lo stesso stato interno e la stessa UI di snapshot, quindi la chiusura e' diventata piu regolare e veloce.

### Sprint 3

La discesa e' stata piu ripida per effetto dell'elevato riuso architetturale:

- il Command pattern ha trasformato sei User Stories di undo in variazioni dello stesso schema;
- i tag hanno fornito dati riusabili dalle playlist automatiche;
- facade, service e repository erano gia pronti;
- i refactoring precedenti avevano ridotto l'impatto delle modifiche tra layer;
- i test hanno permesso di validare rapidamente comportamenti delicati senza dover fare solo prove manuali.

Per questo motivo l'ultimo burndown non rappresenta lavoro superficiale o stime troppo basse, ma l'effetto di una base tecnica che ha reso economiche molte estensioni.

---

## 10. Quality and testing

La qualita e' cresciuta progressivamente.

In Sprint 2 la suite Maven contava 82 test superati, con forte copertura sul playback. In Sprint 3 la suite finale arriva a **184 test superati**, con **0 failure** e **0 error**.

Le aree maggiormente coperte sono:

- command concreti e undo;
- `UndoManager` e `CommandFactory`;
- facade e integrazione delle operazioni annullabili;
- `AutoPlaylistService`;
- `TagService` e `SqliteTagRepository`;
- `PlaybackService`;
- `PlaylistService`;
- model e strategy.

L'aumento dei test e' coerente con la crescita del rischio: undo e tag toccano piu layer e richiedono ripristino corretto dello stato, associazioni e ordine.

---

## 11. Final assessment

Il progetto finale dimostra un'evoluzione incrementale coerente.

Sprint 1 ha dato al team una prima applicazione funzionante. Sprint 2 ha trasformato quella base in un'architettura piu pulita e testabile. Sprint 3 ha sfruttato il lavoro precedente per completare feature avanzate e chiudere la release con maggiore stabilita.

Il valore principale del percorso non e' solo nei 144 Story Points completati, ma nel modo in cui sono stati completati: il team ha usato refactoring e design pattern come strumenti per ridurre complessita reale, non come elementi decorativi.

La release finale include:

- gestione completa del catalogo;
- gestione completa delle playlist;
- playback simulato con modalita avanzate;
- undo globale sulle principali mutazioni;
- tag visuali;
- playlist automatiche;
- persistenza SQLite;
- architettura layered;
- test automatici finali superati.

Il Music Playlist Manager e' quindi pronto per la demo finale come prodotto funzionante, tracciabile e sostenuto da scelte architetturali motivate.
