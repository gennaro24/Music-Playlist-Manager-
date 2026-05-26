# Music Playlist Manager
A Desktop Application for managing Music Tracks, Playlists, Playback and Advanced Filters, developed in Java. The process of development of the application will follow a SCRUM Procedure.

## Main Feature
- To be defined..

## Technologies
- **Language:** Java 17
- **Build Tool:** Maven
- **Testing:** JUnit 5
- **SCRUM tool:** Trello

## Project Structure

- `pom.xml`
  - Maven configuration file.
  - Defines Java version, dependencies, plugins, build and test configuration.

- `src/`
  - Contains the application source code and test code.

- `src/main/java/`
  - Contains the main Java source code of the application.

- `src/main/java/it/unisa/sad/playlistmanager/`
  - Base package of the project.

- `src/main/java/it/unisa/sad/playlistmanager/Main.java`
  - Application entry point.

- `src/test/java/`
  - Contains JUnit test classes.

- `src/test/java/it/unisa/sad/playlistmanager/MainTest.java`
  - Initial test class used to verify that the Maven/JUnit setup works correctly.

- `docs/`
  - Contains project documentation and Scrum artifacts.

- `.gitignore`
  - Specifies files and folders that must not be tracked by Git, such as generated build files and IDE-specific files.

- `.gitattributes`
  - Defines Git rules for consistent handling of text files and line endings.

- `.editorconfig`
  - Defines common formatting rules for editors and IDEs.

- `.vscode/`
  - Contains optional VS Code workspace configuration.

- `.vscode/extensions.json`
  - Recommends useful VS Code extensions for Jav and   Maven development.

## How To Use the Repository
- After Cloning the repo, assure that you have installed the correct version of Java, and you have Maven.
- First, always open the project root folder `sad-project-work`.
- Run in the Terminal `mvn clean test`. You should be positioned in the root directory to succeed the build.
- To Run the application from VS Code by opening the  `Main.java`. You should be able to run the code by clicking on the run button. To avoid problems, are suggested extension in Vs Code:
    - "vscjava. vscode-java-pack",
    - "vscjava.vscode-maven",
    - "redhat.java" 

## Development Rules
- Always open the project in the root folder.
- Do not commit generated files.
- Run `mvn clean test` before pushing.
- Work on feature branches.


## Project Docs
- `docs/User-Stories.md`
  - Contains the product backlog with all the user stories. 
- `docs/Dod.md`
  - Contains the Definition Of Done.
## Project links    
Trello: https://trello.com/b/hoal9I6i/scrum-process-sad-25-26-gruppo14