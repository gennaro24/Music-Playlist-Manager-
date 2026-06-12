# Project Planning — Music Playlist Manager

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design — A.Y. 2025/2026  
**Process:** Scrum  
**Planning status:** Sprint 2 Planning  
**Current delivery:** Sprint 2 Release — 11 June 2026

---

## 1. Project Overview

Music Playlist Manager is a Java desktop application for managing a music catalog, playlists, simulated playback and advanced filters.

The project is developed with Scrum. The team works through time-boxed increments and maintains the required artifacts in the GitHub repository and in the external tracking tools used during the course.

Main technologies:

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build tool | Maven |
| UI | JavaFX / FXML |
| Persistence | SQLite |
| Testing | JUnit 5 |
| Process tracking | Trello + spreadsheet |
| Version control | Git + GitHub |

---

## 2. Delivery Roadmap

| Date | Phase | Main expected output |
|---|---|---|
| 28 May | Pre-game | Initial backlog, Sprint 1 planning, architecture description, first presentation |
| 4 June | Sprint 1 Release | First working increment, updated artifacts, review and retrospective |
| 11 June | Sprint 2 Release | Second working increment, updated artifacts, review and retrospective |
| 18 June | Final Release | Final increment, updated artifacts, final presentation |

---

## 3. Pre-game Summary

The Pre-game phase established the initial product vision, the Scrum setup and the architectural direction of the system.

### Work completed

| Area | Result |
|---|---|
| Product scope | User epics were transformed into initial user stories with priorities, estimates and acceptance criteria. |
| Product Backlog | Initial Product Backlog created with catalog, playlist, playback and advanced filtering stories. |
| Definition of Done | Definition of Done created for build, tests, architecture compliance, acceptance criteria and Scrum traceability. |
| Architecture | Initial MVC + layered Model architecture defined. |
| Repository setup | GitHub repository initialized with Maven project structure and documentation folders. |
| Scrum setup | Trello board and spreadsheet prepared for sprint tracking. |
| Sprint 1 planning | Sprint 1 scope selected to produce a first thin working slice. |

### Pre-game deliverables

- `docs/PreGame/ProductBacklog.md`
- `docs/PreGame/DoD.md`
- `docs/PreGame/Architecture.md`
- `docs/Sprint1/SprintBacklog.md`
- `docs/Planning.md`
- Initial 5-minute presentation

---

## 4. Sprint 1 Summary

Sprint 1 focused on building the first usable increment of the application. The goal was to validate the architecture through a thin slice covering catalog management, playlist creation, playlist content management and basic simulated playback.

### Sprint 1 goal

Deliver a first working version of the Music Playlist Manager that provides the base architecture and allows the user to manage basic tracks and playlists, play a single track and pause a single track.

### Main implemented functions

| Area | High-level functions implemented |
|---|---|
| Project structure | Maven setup, package structure, JavaFX resources and initial FXML loading. |
| Catalog | Add track, view catalog. |
| Playlist management | Create playlist, view playlist content, add tracks to playlist, remove tracks from playlist. |
| Playback | Start playback of a single track, pause playback of a single track. |
| Persistence | Initial SQLite schema and repository implementations. |
| Testing | JUnit tests for main domain/service behavior introduced during Sprint 1. |

### Architectural result

The project adopted the following structure:

```text
ui/controller + ui/view
→ application/facade + application/service
→ domain/model
→ persistence/repository + persistence/db
```

The main design rule was that the UI must not contain business logic and services must depend on repository abstractions instead of concrete persistence details.

### Sprint 1 deliverables

- Sprint 1 working increment
- Updated Product Backlog
- Sprint 1 Review Report
- Sprint 1 Retrospective Report
- Project Burndown Chart update
- 5-minute Sprint 1 demo

### Main issue discovered

During Sprint 1, a technical debt emerged: part of the object creation logic was concentrated inside the JavaFX main controller. This increased coupling between UI, services, repositories and database initialization. Sprint 2 includes a dedicated technical story to fix this issue.

---

## 5. Sprint 2 Planning

Sprint 2 continues from the Sprint 1 increment and focuses on three priorities:

1. complete the core catalog and playlist management operations;
2. improve the playback engine with sequential, skip, shuffle and loop behavior;
3. reduce architectural technical debt through controller dependency injection and centralized bootstrapping.

### Sprint 2 goal

Consolidate the core business logic of the Music Playlist Manager by implementing advanced track management, playlist deletion and the main simulated playback mechanisms, while decoupling JavaFX controllers from infrastructure creation.

---

## 6. Sprint 2 Selected User Stories

| ID | User Story | Story Points | Priority |
|---|---|---:|---|
| US-Tech 01 | Refactor instance lifecycle and decouple UI from infrastructure creation | 5 | High / Blocking |
| US-03 | Modify an existing track | 3 | High |
| US-04 | Delete a track from the catalog | 5 | High |
| US-5.1 | Delete an existing playlist | 3 | High |
| US-10 | Pause a single track | 3 | Medium |
| US-11 | Enable loop mode on a single track | 2 | Medium |
| US-12 | Start sequential playback of a playlist/catalog | 3 | High |
| US-13 | Pause a playlist | 3 | Medium |
| US-14 | Skip to the next track during playback | 2 | High |
| US-15 | Play a playlist/catalog in shuffle mode | 8 | High |
| US-16 | Enable loop mode on a playlist/catalog | 3 | Medium |
| US-17 | View current track and playback state | 3 | High |
| **Total** | **11 functional user stories + 1 technical user story** | **43** |  |

The detailed technical task breakdown is tracked in the Sprint 2 spreadsheet:

[Open Sprint 2 Task Breakdown](https://docs.google.com/spreadsheets/d/10jyFUr_pTY-9O31ru3sLkAWDPT1Td74MkxeRBL9q9p8/edit?gid=1014741886#gid=1014741886)

---

## 7. Sprint 2 High-Level Implementation Plan

### Architectural refactoring

- Move service, repository and database creation out of JavaFX controllers.
- Introduce a centralized bootstrapper or application factory.
- Configure JavaFX controller creation through dependency injection or controller factory.
- Keep controllers dependent only on the facade or application services.
- Update the architecture document with the new instance lifecycle.

### Catalog and playlist management

- Implement update of existing tracks.
- Implement deletion of tracks from the catalog.
- Keep playlist references consistent after track deletion.
- Implement playlist deletion.
- Refresh the UI after update/delete operations.

### Playback engine

- Implement sequential playback for playlist/catalog.
- Implement pause for both single-track and playlist playback.
- Implement skip to next track.
- Implement single-track loop.
- Implement playlist/catalog loop.
- Implement shuffle playback.
- Expose current track, playback state and playback mode to the UI.

### Testing

- Add JUnit tests for track update/delete and playlist deletion.
- Add JUnit tests for playback state transitions.
- Add JUnit tests for sequential, skip, shuffle and loop behavior.
- Run regression tests to verify that Sprint 1 features still work.
- Run `mvn clean test` before each Pull Request.

---

## 8. Sprint 2 Deliverables Checklist

### Documentation and Scrum artifacts

- [X] `docs/Sprint2/SprintBacklog.md`
- [X] Updated `docs/PreGame/ProductBacklog.md`, if backlog refinements are made
- [X] Updated `docs/PreGame/Architecture.md`, especially the instance lifecycle and dependency wiring section
- [X] `docs/Sprint2/Review-Report.md`
- [X] `docs/Sprint2/Retrospective-Report.md`
- [X] Updated Project Burndown Chart
- [X] 5-minute Sprint 2 presentation/demo notes

### Product increment

- [X] Application starts correctly after architectural refactoring
- [X] Sprint 1 functions still work
- [X] Track update/delete available from UI and services
- [X] Playlist deletion available from UI and services
- [X] Sequential playback, pause and skip available
- [X] Shuffle and loop behavior implemented at least at core logic level
- [X] Current track and playback state visible in the UI
- [X] `mvn clean test` passes

---

## 9. Sprint 2 Demo Plan

The Sprint 2 demo should show a coherent user flow instead of isolated implementation details.

Suggested order:

1. Show that the application still starts correctly after the refactoring.
2. Briefly explain that controllers no longer create repositories/services directly.
3. Modify a track and show the updated catalog.
4. Delete a track and show that playlist references remain consistent.
5. Delete a playlist.
6. Start sequential playlist playback.
7. Pause, skip and show the current track/state update.
8. Show shuffle or loop behavior if stable enough for the demo.

---

## 10. Repository Organization

Expected documentation structure after Sprint 2:

```text
docs/
├── PreGame/
│   ├── ProductBacklog.md
│   ├── DoD.md
│   └── Architecture.md
├── Sprint1/
│   ├── SprintBacklog.md
│   └── Sprint-Retrospective-Report.md
├── Sprint2/
│   ├── SprintBacklog.md
│   ├── Review-Report.md
│   ├── Retrospective-Report.md
│   └── Burndown-Chart.png
└── Planning.md
```

---

## 11. Branching and Integration Rules

- Work on dedicated feature branches.
- Open Pull Requests toward `main`.
- Keep `main` stable.
- Avoid mixing unrelated user stories in the same branch.
- Run `mvn clean test` before opening or updating a Pull Request.
- Request code review before merging.
- Update documentation in the same PR only when the implementation affects architecture, planning or usage.

