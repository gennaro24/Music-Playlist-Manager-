# Sprint 2 Backlog — Music Playlist Manager

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design — A.Y. 2025/2026  
**Process:** Scrum  
**Sprint:** Sprint 2  
**Delivery date:** 11 June 2026  
**Planning status:** Sprint 2 Planning

---

## Sprint Goal

Consolidate the core business logic of the Music Playlist Manager by implementing advanced track management, playlist deletion and the main simulated playback mechanisms, while reducing the technical debt introduced in Sprint 1 by decoupling JavaFX controllers from service, repository and database instantiation.

The expected Sprint 2 increment must provide a more maintainable architecture and a more complete playback core: sequential playback, pause, skip, shuffle, loop and current playback state visualization.

---

## Sprint Scope

| ID | User Story | Priority | Story Points | Status |
|---|---|---:|---:|---|
| US-Tech 01 | Refactor instance lifecycle and decouple UI from infrastructure creation | High / Blocking | 5 | DONE |
| US-03 | Modify an existing track | High | 3 | DONE |
| US-04 | Delete a track from the catalog | High | 5 | DONE |
| US-5.1 | Delete an existing playlist | High | 3 | DONE |
| US-10 | Pause a single track | Medium | 3 | DONE |
| US-11 | Enable loop mode on a single track | Medium | 2 | DONE |
| US-12 | Start sequential playback of a playlist/catalog | High | 3 | DONE |
| US-13 | Pause a playlist | Medium | 3 | DONE |
| US-14 | Skip to the next track during playback | High | 2 | DONE |
| US-15 | Play a playlist/catalog in shuffle mode | High | 8 | DONE |
| US-16 | Enable loop mode on a playlist/catalog | Medium | 3 | DONE |
| US-17 | View current track and playback state | High | 3 | DONE |
| **Total** | **11 functional user stories + 1 technical user story** |  | **43** |  |

## Architectural Focus — Technical Debt Resolution

During Sprint 1, part of the object creation logic was concentrated inside the main JavaFX controller. This created unnecessary coupling between the presentation layer and infrastructure details such as services, repositories and database initialization.

Sprint 2 introduces **US-Tech 01** to move object creation into a dedicated application bootstrap mechanism. Controllers must receive already configured dependencies instead of creating repositories, services or database utilities directly.

Expected architectural direction:

```text
Main / Bootstrapper
→ DatabaseConnectionManager / DatabaseInitializer
→ Repository implementations
→ Application services
→ MusicPlaylistManagerFacade
→ JavaFX controllers through dependency injection / controller factory
```

Architectural rule for Sprint 2:

```text
Controllers must depend on the facade or application services, not on SQLite repositories or database utilities.
```

---

## User Stories Breakdown — Tasks

The user stories selected for Sprint 2 have been split into more specific technical tasks.

The complete Sprint 2 task breakdown is available in the following spreadsheet:

[Open Sprint 2 Task Breakdown](https://docs.google.com/spreadsheets/d/10jyFUr_pTY-9O31ru3sLkAWDPT1Td74MkxeRBL9q9p8/edit?gid=1014741886#gid=1014741886)

The spreadsheet includes, for each task:

- task ID;
- related user story;
- task title;
- assigned team member;
- current status;
- estimated effort;
- actual time spent.

---


