# Software Architecture - Music Playlist Manager

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design - A.Y. 2025/2026
**Architecture style:** Model-View-Controller (MVC) with a layered Model  
**Main goal:** separate user interface, application logic, domain logic and persistence, so that the system is easier to test, maintain and evolve during the Scrum sprints.

---

## 1. High-Level Architecture

The Music Playlist Manager is a Java desktop application for managing music tracks, playlists and simulated playback. The architecture combines the **Model-View-Controller (MVC)** pattern with a **layered internal Model** and a centralized application bootstrap.

![High-Level Architecture](imgs/High-Level-Architecture.jpeg)

At a high level, the system is divided into five main areas:

1. **Bootstrap Module**
   Creates and connects database utilities, repository implementations, application services, facade and JavaFX controllers.

2. **Presentation Layer**
   Manages interaction with the user. It contains the FXML views and the controllers that receive UI events.

3. **Application Module**
   Coordinates application use cases. It exposes services and a facade used by the controllers and uses commands to represent undoable catalog and playlist mutations. It does not contain UI code.

4. **Domain Module**
   Contains the core business concepts and playback rules, such as tracks, playlists, playback queues, state snapshots and playback strategies.

5. **Persistence Module**
   Isolates data access. It contains repository abstractions, SQLite repository implementations and database utilities.

The main runtime dependency flow is:

```text
Main
-> AppFactory / SqliteAppFactory
-> Repository implementations and Application Services
-> CommandFactory and UndoManager
-> MusicPlaylistManagerFacade
-> ControllerFactory
-> JavaFX Controllers
-> FXML Views
```

The use-case flow is:

```text
User
-> View
-> Controller
-> Application Facade
-> CommandFactory / UndoManager (for undoable mutations)
-> Command
-> Application Service
-> Domain Model / Playback Strategy
-> Repository Interface
-> SQLite Repository Implementation
-> Database
```

The architectural rule is that controllers depend on the application facade and do not create database managers, repositories or services. Application services coordinate domain objects and depend on repository interfaces rather than concrete SQLite implementations. Domain objects remain independent from UI and persistence details.

---

## 2. MVC Architectural Choice

The project adopts **MVC** to separate graphical interaction from application behavior.

### View

The View is defined mainly through JavaFX FXML resources. Its responsibility is to display data and collect user input. The views show the music catalog, playlists, current track, playback state, playback mode, elapsed time and operation feedback.

The View does not decide business behavior. For example, it must not decide whether a playlist name is duplicated, whether a track can be removed, or which track comes next during playback.

### Controller

The Controller receives UI events, such as button clicks or form submissions, and translates them into calls to `MusicPlaylistManagerFacade`. It updates the View using domain objects and `PlaybackSnapshot` values returned by the application layer.

Controllers receive the facade through constructor injection performed by `ControllerFactory`. They must not directly access SQLite repositories, `DatabaseConnectionManager` or application service implementations.

`MainViewController` also coordinates the included track, playlist and playback controllers at presentation level. This coordination concerns UI events and layout updates, not business rules.

### Model

In this project, the Model is not a single class or package. It is organized as a layered Model composed of:

```text
Application Module
Domain Module
Persistence Module
```

This choice keeps the UI focused on presentation and allows the main application and domain logic to be tested with JUnit without launching the full graphical interface.

---

## 3. Package Structure

```text
it.unisa.sad.playlistmanager
|
|-- Main.java
|
|-- bootstrap
|   |-- AppFactory
|   |-- SqliteAppFactory
|   `-- ControllerFactory
|
|-- ui
|   |-- view (FXML resources)
|   |   |-- MainView.fxml
|   |   |-- TrackView.fxml
|   |   |-- PlaylistView.fxml
|   |   `-- PlaybackView.fxml
|   |
|   `-- controller
|       |-- MainViewController
|       |-- TrackController
|       |-- PlaylistController
|       `-- PlaybackController
|
|-- application
|   |-- command
|   |   |-- Command
|   |   |-- CommandFactory
|   |   |-- UndoManager
|   |   `-- concreteCommands
|   |       |-- AddTrackCommand
|   |       |-- DeleteTrackCommand
|   |       |-- CreatePlaylistCommand
|   |       |-- DeletePlaylistCommand
|   |       |-- AddTrackToPlaylistCommand
|   |       `-- RemoveTrackFromPlaylistCommand
|   |
|   |-- facade
|   |   `-- MusicPlaylistManagerFacade
|   |
|   |-- service
|   |   |-- TrackService
|   |   |-- PlaylistService
|   |   `-- PlaybackService
|   |
|   `-- exceptions
|       |-- ValidationException
|       |-- TrackNotFoundException
|       `-- PlaylistNotFoundException
|
|-- domain
|   |-- model
|   |   |-- Track
|   |   |-- Playlist
|   |   |-- PlaybackQueue
|   |   |-- PlaybackSnapshot
|   |   |-- PlaybackState
|   |   |-- PlaybackMode
|   |   `-- PlaybackSource
|   |
|   |-- strategy
|   |   |-- PlaybackStrategy
|   |   |-- SequentialPlaybackStrategy
|   |   |-- ShufflePlaybackStrategy
|   |   `-- RepeatAllPlaybackStrategy
|   |
|   `-- exceptions
|       `-- ValidationException
|
`-- persistence
    |-- repository
    |   |-- TrackRepository
    |   |-- PlaylistRepository
    |   |-- SqliteTrackRepository
    |   `-- SqlitePlaylistRepository
    |
    |-- db
    |   |-- DatabaseConnectionManager
    |   `-- DatabaseInitializer
    |
    `-- exceptions
        `-- RepositoryException
```

---

## 4. Modules and Responsibilities

### 4.1 Root Package

The root package contains the JavaFX application entry point.

**Responsibility:** start the application, request the configured facade from the application factory, configure the JavaFX controller factory and open the main user interface.

`Main.java` acts as the application composition entry point. It remains free from business rules and UI event logic.

### 4.2 Bootstrap Module - `bootstrap`

The `bootstrap` package centralizes instance creation and dependency composition.

```text
bootstrap
|-- AppFactory
|-- SqliteAppFactory
`-- ControllerFactory
```

#### `AppFactory` and `SqliteAppFactory`

**Responsibility:** build the configured application object graph.

`SqliteAppFactory` creates:

- `DatabaseConnectionManager`;
- `DatabaseInitializer`;
- SQLite repository implementations;
- `TrackService`, `PlaylistService` and `PlaybackService`;
- `CommandFactory` and the shared `UndoManager`;
- `MusicPlaylistManagerFacade`.

Repository implementations are assigned to repository interface references before being injected into services. This keeps application services independent from SQLite classes.

#### `ControllerFactory`

**Responsibility:** integrate JavaFX controller creation with manual dependency injection.

`Main` registers `ControllerFactory` through `FXMLLoader.setControllerFactory(...)`. The factory creates controllers and supplies the shared `MusicPlaylistManagerFacade` through their constructors. Controllers that do not require the facade may use a no-argument constructor.

This package is the composition root of the application. Infrastructure creation must not be moved back into UI controllers.

### 4.3 Presentation Layer - `ui`

The `ui` module contains everything related to the desktop interface.

```text
ui
|-- view
`-- controller
```

#### `ui.view`

**Responsibility:** display information and collect user input.

The concrete views are JavaFX FXML resources. They show tracks, playlists, playback information and feedback messages and provide controls for catalog, playlist and playback operations.

FXML resources must not contain business rules.

#### `ui.controller`

**Responsibility:** receive UI events and delegate operations to the application facade.

Controllers convert user input into facade calls and update JavaFX controls with the returned results. They may contain presentation-specific behavior such as table configuration, dialog handling, formatting and view refresh.

Controllers must not:

- create repositories, services or database utilities;
- execute SQL;
- select the next playback track;
- implement application validation rules that belong to services or domain objects.

### 4.4 Application Layer - `application`

The `application` module coordinates use cases and acts as the boundary between UI, domain and persistence abstractions.

```text
application
|-- command
|-- facade
|-- service
`-- exceptions
```

#### `application.command`

**Responsibility:** represent undoable catalog and playlist mutations as command objects and manage their execution history.

`Command` defines the `execute()` and `undo()` contract. `CommandFactory` creates concrete commands with the shared application services required by each use case. `UndoManager` executes commands, stores successfully executed commands in a LIFO stack and undoes the most recent command.

Concrete commands contain only the state required to reverse their own operation. Commands that delete data preserve snapshots such as the deleted entity, playlist memberships and original track positions. They delegate persistence changes to `TrackService` and `PlaylistService`; they do not execute SQL directly.

#### `application.facade`

**Responsibility:** provide a simplified and unified entry point for controllers.

`MusicPlaylistManagerFacade` routes undoable catalog and playlist mutations through `CommandFactory` and `UndoManager`. Read operations, track metadata updates and playback operations continue to delegate directly to their specialized services.

The facade also coordinates operations involving playback. Deleting a track or playlist stops or updates playback when the active source is affected. Undo restores catalog and playlist data but intentionally does not resume the previous playback session.

The facade should coordinate use cases but should not absorb domain rules that belong in domain objects or specialized services.

#### `application.service`

**Responsibility:** implement and coordinate application use cases.

- `TrackService` manages track creation, retrieval, update and deletion.
- `PlaylistService` manages playlist creation, retrieval, deletion and track membership.
- `PlaybackService` manages simulated playback state, queue progression, modes, elapsed time and strategy selection.

Services depend on repository interfaces. They do not contain JavaFX code and do not instantiate concrete repositories.

#### `application.exceptions`

**Responsibility:** expose errors meaningful to application use cases.

`ValidationException`, `TrackNotFoundException` and `PlaylistNotFoundException` allow controllers to present coherent feedback without depending on SQL exceptions or persistence details.

### 4.5 Domain Layer - `domain`

The `domain` module contains the core business concepts and playback algorithms.

```text
domain
|-- model
|-- strategy
`-- exceptions
```

#### `domain.model`

**Responsibility:** represent entities, playback state and domain invariants.

- `Track` and `Playlist` represent the main catalog entities.
- `PlaybackQueue` stores an immutable track list, current index and playback source.
- `PlaybackSource` distinguishes single-track, playlist and catalog playback.
- `PlaybackState` and `PlaybackMode` represent current player state and selected mode.
- `PlaybackSnapshot` is an immutable record used by the UI to read current track, state, mode and elapsed time.

The domain model remains independent from JavaFX and SQLite.

#### `domain.strategy`

**Responsibility:** encapsulate algorithms used to determine the next track.

`PlaybackStrategy` defines the common contract. Current implementations are:

- `SequentialPlaybackStrategy`, which advances in the original queue order;
- `ShufflePlaybackStrategy`, which uses a shuffled queue;
- `RepeatAllPlaybackStrategy`, which restarts from the first track after the last one.

`PlaybackService` acts as the Strategy context and selects the appropriate implementation according to `PlaybackMode`. Single-track repetition (`REPEAT_ONE`) is currently coordinated directly by the playback service because it does not require moving to another queue element.

#### `domain.exceptions`

**Responsibility:** report violations of entity invariants, such as invalid track metadata or playlist state.

### 4.6 Persistence Layer - `persistence`

The `persistence` module isolates storage and database access from the rest of the application.

```text
persistence
|-- repository
|-- db
`-- exceptions
```

#### `persistence.repository`

**Responsibility:** define and implement data access operations.

`TrackRepository` and `PlaylistRepository` define the operations required by application services. `SqliteTrackRepository` and `SqlitePlaylistRepository` implement those contracts using SQLite.

The application layer depends on repository interfaces, not directly on concrete SQLite classes.

#### `persistence.db`

**Responsibility:** manage database connection and schema initialization.

`DatabaseConnectionManager` centralizes connection creation. `DatabaseInitializer` prepares the required tables and relationships at application startup.

#### `persistence.exceptions`

**Responsibility:** prevent SQL-specific exceptions from leaking into upper layers.

SQLite repositories wrap `SQLException` failures in unchecked `RepositoryException` values with operation-specific context.

---

## 5. Dependency Injection and Instance Lifecycle

The application uses manual constructor injection rather than a dependency injection framework.

The startup sequence is:

```text
1. JavaFX creates Main
2. Main.init() creates SqliteAppFactory through the AppFactory abstraction
3. SqliteAppFactory initializes the database
4. SqliteAppFactory creates repository implementations
5. Repository interfaces are injected into application services
6. TrackService and PlaylistService are injected into CommandFactory
7. SqliteAppFactory creates the shared UndoManager
8. Services, CommandFactory and UndoManager are injected into MusicPlaylistManagerFacade
9. Main creates ControllerFactory with the configured facade
10. FXMLLoader uses ControllerFactory to instantiate JavaFX controllers
11. Controllers receive the same facade through constructor injection
```

This lifecycle guarantees that:

- infrastructure is composed in one place;
- controllers do not know how dependencies are built;
- services can be tested with fake repository implementations;
- controllers can be tested with a fake or specialized facade;
- the application uses one coherent set of service and repository instances;
- all undoable operations in the application session share one in-memory LIFO history.

---

## 6. Main Runtime Flows

### Read or non-undoable catalog operation

```text
User action
-> FXML View captures input
-> Controller receives the event
-> MusicPlaylistManagerFacade delegates the use case
-> TrackService or PlaylistService validates and coordinates
-> Domain objects enforce entity invariants
-> Repository interface persists or retrieves data
-> SQLite repository accesses the database
-> Controller refreshes the View
```

Example: when the user updates a track, `TrackController` sends the request to the facade. `TrackService` validates the request and calls `TrackRepository.update(...)`. The SQLite implementation persists the new metadata, and the controller refreshes the catalog or current playlist view.

### Undoable catalog or playlist mutation

```text
User action
-> Controller
-> MusicPlaylistManagerFacade
-> CommandFactory creates the concrete Command
-> UndoManager executes the Command
-> Command delegates to TrackService or PlaylistService
-> Repository interface
-> SQLite repository
-> UndoManager stores the successful Command
-> Controller refreshes the View
```

The current undoable mutations are track creation and deletion, playlist creation and deletion, adding a track to a playlist and removing a track from a playlist. Each concrete command captures the minimum data required by its `undo()` operation, including original playlist positions when order must be restored.

### Undo operation

```text
User undo action
-> Controller
-> MusicPlaylistManagerFacade.undoLastAction()
-> UndoManager selects the most recent Command
-> Command.undo()
-> Application Service
-> Repository interface
-> SQLite repository
-> UndoManager removes the restored Command from history
```

The history is maintained only in memory and lasts for the current application session. The current implementation supports undo but not redo.

### Playback operation

```text
User playback command
-> PlaybackController
-> MusicPlaylistManagerFacade
-> PlaybackService
-> PlaybackQueue + PlaybackStrategy
-> PlaybackSnapshot
-> PlaybackController updates the View
```

The UI does not decide which track comes next. `PlaybackService` maintains state and elapsed time, while the selected strategy calculates queue progression. The UI reads the resulting state through `PlaybackSnapshot`.

### Error propagation

```text
SQLException
-> RepositoryException
-> Application Service / Facade
-> Controller feedback
```

Domain validation errors originate in domain objects, while missing resources and use-case validation are represented by application exceptions. Controllers translate these exceptions into user-facing messages.

---

## 7. Applied Patterns and Principles

The current architecture applies the following patterns:

- **MVC:** separates FXML views, JavaFX controllers and the layered Model.
- **Facade:** provides controllers with one application entry point.
- **Command:** represents reversible mutations and stores the state required to undo them.
- **Repository:** separates application logic from SQLite persistence.
- **Strategy:** encapsulates playback queue progression algorithms.
- **Factory / Composition Root:** centralizes application instance creation and concrete command creation.
- **Dependency Injection:** supplies repositories to services and the facade to controllers.
- **Immutable DTO:** `PlaybackSnapshot` exposes playback state without giving the UI ownership of service state.

The main design principles are:

- Separation of Concerns;
- Dependency Inversion;
- high cohesion and low coupling;
- domain and application logic outside the UI;
- dependence on abstractions at service boundaries;
- explicit and testable playback state.

---
