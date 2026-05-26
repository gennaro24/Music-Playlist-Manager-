# Definition of Done (DoD)

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design — A.Y. 2025/2026  
**Artifact type:** Official Group Artifact

A User Story is considered completed, therefore movable to **Done** and presentable in the end-of-Sprint demo, only when it satisfies all the following criteria.

## 1. Code Quality and Build Constraints

A User Story is **Done** only if the implemented code satisfies the build, design, and code quality constraints.

### Build correctness

- The Java code compiles correctly.
- The project is built using the adopted build tool, namely Maven.
- The build must finish without errors.
- Any critical warnings must be analyzed and fixed before the merge.

### Architectural compliance

- New classes must respect the architecture agreed upon by the team.
- Business logic must not be implemented directly in the UI layer.
- Classes must maintain high cohesion and low coupling.
- The implementation must respect the responsibilities assigned to packages/modules.
- Design patterns must be applied only if agreed upon by the team and genuinely useful for the specific problem.

### Clean code and maintainability

- The code must avoid unnecessary duplication, respecting the DRY principle.
- Java naming conventions must be followed.
- Class, method, and variable names must be clear and meaningful.
- Methods that are too complex must be split into smaller and more readable methods.
- Magic numbers and hard-coded constants must be avoided or extracted into constants with meaningful names.
- Comments must explain non-obvious design decisions, not repeat what the code already says.

### Internal code review

- Before merging into the main branch, the implementation must be reviewed by another team member.
- The review checks code readability, architectural consistency, tests, and the absence of obvious technical debt.
- Problems found during the review must be fixed or explicitly tracked before considering the story Done.

## 2. Software Engineering and Automated Testing

A User Story is **Done** only if the implemented functionality is covered by automated tests, where applicable.

### JUnit testing

- Unit tests must be implemented with JUnit.
- Testing must not be based only on manual checks.
- All new business logic classes must have automated tests covering their public behavior.
- All existing tests and new tests must run successfully.

### Test coverage

The JUnit test suite must cover:

- Standard valid cases.
- Invalid inputs.
- Boundary values.
- Error conditions.
- Regression cases connected to already implemented functionality.

Examples of required checks:

- Empty or missing strings.
- Invalid durations.
- Invalid release years.
- Duplicate playlist names.
- Duplicate tracks inside a playlist.
- Operations on empty playlists or catalogs.

### Absence of regressions

- The implementation must not break already completed User Stories.
- The command `mvn clean test` must run successfully before moving the story to Done.

## 3. Functional Verification and Acceptance Criteria

A User Story is **Done** only if it satisfies the expected functional behavior.

### Acceptance Criteria

- All Acceptance Criteria defined for the User Story must be satisfied.
- Each Given / When / Then scenario must be verified manually or covered by automated tests where possible.
- The implemented functionality must respect the value expressed in the User Story: As a ... I want ... So that ...

### Functional correctness

- The functionality must not cause application crashes.
- The functionality must not introduce inconsistent states in the domain.
- The functionality must correctly handle the expected error cases.
- The user must receive understandable feedback when an operation is invalid.

### UI verification

If the User Story modifies or involves the user interface:

- The UI must allow the user to use the functionality.
- The UI must show updated data after the operation.
- The main interface elements must not be broken, overlapping, or unusable.
- The UI must remain consistent with the current state of the application.

## 4. Scrum Process Traceability

A User Story is **Done** only if the process information is updated and traceable.

### Trello tracking

- The Trello card associated with the User Story must be updated.
- Assigned members must be indicated.
- Related technical tasks must be visible.
- The actual time spent on tasks must be recorded.
- Completed tasks must not be deleted, but moved or marked as completed.

### Synchronization with GitHub

- The code must be pushed to GitHub.
- The implementation must be integrated through an approved Pull Request.
- The main branch must remain stable after the merge.
- After integration, the project must compile and all tests must pass.

### Documentation update

When the User Story modifies architecture, backlog, or usage modes:

- Relevant documentation must be updated.
- Important design decisions must be briefly documented.
- Any known limitations must be explicitly tracked.

## 5. Product Owner Approval

Since during the Pre-game and the Sprints the team collectively acts as Product Owner, a User Story is **Done** only after internal validation.

### Internal validation

- The implemented functionality must be shown during an internal Sprint Review.
- The team must verify that the planned requirements have been satisfied.
- The team must agree that the functionality is ready for the Sprint demo.
- If relevant problems remain, the User Story must stay In Progress or be marked as partially completed, not Done.

## Final checklist for moving a User Story to Done

- The code compiles correctly.
- The command `mvn clean test` passes.
- The new business logic is covered by JUnit tests.
- The Acceptance Criteria are satisfied.
- There are no known blocking bugs.
- The UI is consistent and usable, if involved.
- The implementation respects the agreed architecture.
- Code quality principles are respected.
- The Pull Request has been reviewed and approved.
- Trello tasks, assignees, and time spent are updated.
- Relevant documentation is updated.
- The team has approved the story during the internal review.
