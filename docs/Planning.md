# Project Planning — Music Playlist Manager

Project: Music Playlist Manager

Course: Software Architecture Design — A.Y. 2025/2026

Planning status: **Sprint 2 Planning**

---

## Sprint 2 Scope & Goal

> **Sprint Goal:** Consolidare il core business logico (gestione tracce avanzata, rimozione playlist) e implementare i meccanismi core del motore di riproduzione (shuffling, looping, playback state), sanando il debito tecnico di accoppiamento infrastrutturale della UI tramite il disaccoppiamento del ciclo di vita delle istanze.

---

##  Sprint 2 Backlog & Estimation

Ecco il quadro completo dei requisiti da implementare in questo Sprint, comprensivo delle stime (Story Points) concordate.

| ID Story | Titolo / Descrizione | Story Points | Priorità |
| --- | --- | --- | --- |
| **US-Tech 01** | **Rifattorizzazione ciclo di vita istanze e disaccoppiamento UI (Refactoring Architetturale)** | **5** | **Alta (Bloccante)** |
| **US-03** | Modificare una traccia esistente | 3 | Alta |
| **US-04** | Eliminare una traccia dal catalogo | 5 | Alta |
| **US-5.1** | Eliminazione playlist *(New User Story)* | 3 | Alta |
| **US-10** | Mettere in pausa una singola traccia | 3 | Media |
| **US-11** | Traccia Singola in Modalità Loop | 2 | Media |
| **US-12** | Riproduzione sequenziale della playlist/catalogo | 3 | Alta |
| **US-13** | Pausa della Playlist | 3 | Media |
| **US-14** | Saltare alla traccia successiva (Skip) | 2 | Alta |
| **US-15** | Riproduzione shuffle playlist/catalogo | 8 | Alta |
| **US-16** | Riproduzione Playlist/catalogo in loop | 3 | Media |
| **US-17** | Visualizzare la traccia corrente e lo stato del playback | 3 | Alta |
| **TOTALE** | **11 Functional US + 1 Technical US** | **43 SP** |  |

---

##  Focus Architetturale: Risoluzione del Debito Tecnico

Per sanare l'accoppiamento emerso nello Sprint 1 (in cui il controller principale istanziava service, repository e DB), l'architettura a livello di istanze viene rimodellata.

## Checklist per l'11 Giugno — Consegna Sprint 2

### 1. Refactoring & Rimodellazione Istanze

* [ ] Creare il piano di refactoring per estrarre la logica di creazioni istanze dal Controller principale JavaFX.
* [ ] Implementare un *Bootstrapper/Main* centralizzato che configuri lo strato di persistenza e i servizi associati.
* [ ] Configurare l'Iniezione delle dipendenze nei controller di presentazione.
* [ ] Verificare che il disaccoppiamento permetta la scrittura di un test unitario isolato (usando Mockito o stub) sul Controller JavaFX.

### 2. Sviluppo delle Feature del Backlog

* [ ] **Gestione Catalogo (US-03, US-04, US-5.1):** Implementare i metodi di update e delete logico/fisico nel database e nei servizi; aggiornare in tempo reale la UI alla cancellazione/modifica.
* [ ] **Riproduzione Standard (US-10, US-12, US-13, US-14, US-17):** Configurare la macchina a stati del Player (PLAYING, PAUSED, STOPPED) per supportare la riproduzione sequenziale, la pausa e lo skip della traccia corrente.
* [ ] **Logiche di Playback Avanzate (US-11, US-15, US-16):** Sviluppare l'algoritmo di shuffling (es. Fisher-Yates per evitare ripetizioni precoci) e le modalità di loop (singolo elemento vs intera lista).

### 3. Testing & Ingegneria del Software

* [ ] Aggiornare/creare i test di accettazione basati sui criteri *Given/When/Then* per ciascuna delle nuove 11 US.
* [ ] Implementare i test unitari con JUnit per verificare gli algoritmi di Shuffle e Loop.
* [ ] Assicurarsi che l'esecuzione di `mvn clean test` termini con successo prima di ogni operazione di merge su branch principale.

### 4. Gestione Scrum Artifacts

* [ ] Aggiornare la board Trello spostando le card in base all'avanzamento giornaliero.
* [ ] Aggiornare il file Excel con il tracciamento dei task dello Sprint 2 e relative ore spese.
* [ ] Aggiornare il diagramma delle classi o dei componenti nel file d'architettura per riflettere il nuovo meccanismo di avvio ed istanziazione.
* [ ] Compilare il Burndown Chart di fine Sprint 2.
* [ ] Redigere lo **Sprint 2 Review Report** e lo **Sprint 2 Retrospective Report** analizzando le criticità e calcolando la Velocity finale del team.

---

##  File Attesi nel Repository entro l'11 Giugno

Assicurati che la struttura dei file di documentazione rispetti la convenzione per la directory `docs/`:

* [ ] `docs/Sprint2/SprintBacklog.md` *(Questo documento di pianificazione)*
* [ ] `docs/Sprint2/Review-Report.md` *(Risultati della demo interna/review)*
* [ ] `docs/Sprint2/Retrospective-Report.md` *(Analisi dei processi interni al team)*
* [ ] `docs/Sprint2/Burndown-Chart.png` *(Grafico dell'andamento dello sprint)*
* [ ] `docs/PreGame/Architecture.md` *(Aggiornato nella sezione "Istanziazione e Dipendenze" per tracciare il superamento del debito)*
