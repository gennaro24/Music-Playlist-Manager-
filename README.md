# Music Playlist Manager

A desktop application for managing music tracks, playlists, simulated playback and advanced filters, developed in Java.

The project follows a Scrum-based development process and is part of the Software Architecture Design course.

---

## Main Features

- Manage a music catalog with tracks and metadata.
- Create and manage playlists.
- Add existing tracks from the catalog to playlists.
- Remove tracks from playlists without deleting them from the catalog.
- View the complete track catalog.
- View created playlists and their ordered content.
- Simulate playback of a single track.
- Pause the simulated playback of a single track.
- Organize the work using Scrum artifacts, Sprint Backlogs, Trello and GitHub.

---

## Technologies

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build Tool | Maven |
| Testing | JUnit 5 |
| Persistence | SQLite |
| Scrum Tool | Trello |
| Version Control | Git and GitHub |

---

## Project Structure

```text
sad-project-work/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── it/unisa/sad/playlistmanager/
│   │           └── Main.java
│   └── test/
│       └── java/
│           └── it/unisa/sad/playlistmanager/
│               └── MainTest.java
├── docs/
│   ├── PreGame/
│   │   ├── ProductBacklog.md
│   │   ├── DoD.md
│   │   └── Architecture.md
│   ├── Sprint1/
│   │   └── SprintBacklog.md
│   └── Planning.md
├── .gitignore
├── .gitattributes
├── .editorconfig
└── .vscode/
    └── extensions.json
```

---

## Main Files and Folders

- `pom.xml`  
  Maven configuration file. It defines Java version, dependencies, plugins, build and test configuration.

- `src/`  
  Contains the application source code and test code.

- `src/main/java/`  
  Contains the main Java source code of the application.

- `src/main/java/it/unisa/sad/playlistmanager/`  
  Base package of the project.

- `src/main/java/it/unisa/sad/playlistmanager/Main.java`  
  Application entry point.

- `src/test/java/`  
  Contains JUnit test classes.

- `src/test/java/it/unisa/sad/playlistmanager/MainTest.java`  
  Initial test class used to verify that the Maven and JUnit setup works correctly.

- `docs/`  
  Contains project documentation and Scrum artifacts.

- `.gitignore`  
  Specifies files and folders that must not be tracked by Git, such as generated build files and IDE-specific files.

- `.gitattributes`  
  Defines Git rules for consistent handling of text files and line endings.

- `.editorconfig`  
  Defines common formatting rules for editors and IDEs.

- `.vscode/`  
  Contains optional VS Code workspace configuration.

- `.vscode/extensions.json`  
  Recommends useful VS Code extensions for Java and Maven development.

---

## How to Use the Repository

1. Clone the repository.
2. Make sure that Java 17 and Maven are installed.
3. Open the project root folder `sad-project-work`.
4. Run the following command from the project root:

```bash
mvn clean test
```

To run the application from VS Code, open `Main.java` and use the Run button.

Recommended VS Code extensions:

- `vscjava.vscode-java-pack`
- `vscjava.vscode-maven`
- `redhat.java`

---

## Development Rules

- Always open the project from the root folder.
- Do not commit generated files.
- Run `mvn clean test` before pushing.
- Work on feature branches.
- Keep the `main` branch stable.
- Use Pull Requests before merging completed work.
- Update Scrum artifacts when the Sprint scope, tasks or status change.

---

## Project Docs

- [Product Backlog](docs/PreGame/ProductBacklog.md)  
  Contains the initial product backlog with all user stories, priorities, story points and acceptance criteria.

- [Definition of Done](docs/PreGame/DoD.md)  
  Defines the conditions that a user story must satisfy to be considered completed.

- [Software Architecture](docs/PreGame/Architecture.md)  
  Describes the main architecture of the project, including layers, packages, responsibilities and design choices.

- [Sprint 1 Backlog](docs/Sprint1/SprintBacklog.md)  
  Contains the Sprint 1 goal, selected user stories, planned scope and link to the detailed task breakdown.

- [Project Planning](docs/Planning.md)  
  Contains the project delivery plan, pre-game checklist and current planning status.

---

## Useful Project Links

- Trello Board: [Open Board](https://trello.com/b/hoal9I6i/scrum-process-sad-25-26-gruppo14)
- Sprint 1 Task Breakdown: [Open Spreadsheet](https://docs.google.com/spreadsheets/d/10jyFUr_pTY-9O31ru3sLkAWDPT1Td74MkxeRBL9q9p8/edit?gid=803831516#gid=803831516)
