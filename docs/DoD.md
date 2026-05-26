# Definition of Done (DoD)

**Project:** Music Playlist Manager  
**Course:** Software Architecture Design — A.A. 2025/2026  
**Artifact type:** Official Group Artifact  

A User Story is formally considered **Done** and is eligible for the Sprint demo only when all the following quality criteria are satisfied.

---

## 1. Code Quality and Build Constraints

A User Story is Done only if the implemented code satisfies the following coding and design constraints.

### Build correctness

- The Java source code compiles successfully.
- The project build is executed through the adopted build automation tool.
- The build must complete without errors.
- Critical warnings must be analyzed and fixed before merging.

### Architectural compliance

- New classes must respect the agreed software architecture.
- Business logic must not be implemented directly inside the UI layer.
- Classes must preserve **high cohesion** and **low coupling**.
- The implementation must respect the responsibilities assigned to each package/module.
- Design patterns must be applied only when they have been explicitly agreed by the team and are useful for the specific problem.

### Clean code and maintainability

- The code must avoid unnecessary duplication, according to the **Don't Repeat Yourself (DRY)** principle.
- Java naming conventions must be followed consistently.
- Method, class and variable names must be clear and meaningful.
- Complex methods must be split into smaller readable methods when needed.
- Magic numbers and hard-coded constants must be avoided or extracted into named constants.
- Comments must explain non-obvious design decisions, not duplicate what the code already says.

### Internal code review

- Before merging into the main branch, the implementation must pass an internal review by another team member.
- The review checks code readability, architectural consistency, test coverage and absence of evident technical debt.
- Issues found during the review must be fixed or explicitly tracked before the story can be considered Done.

---

## 2. Software Engineering and Automated Testing

A User Story is Done only if the implemented functionality is covered by automated tests where applicable.

### JUnit testing

- Unit tests must be implemented using **JUnit**.
- Testing must not rely only on manual checks.
- All new business-logic classes must have automated tests for their public behavior.
- Existing tests and newly added tests must pass successfully.

### Test coverage scope

The JUnit test suite must cover:

- Standard valid cases.
- Invalid input cases.
- Boundary values.
- Error conditions.
- Regression cases related to previously implemented behavior.

Examples of required checks include:

- Empty or missing strings.
- Invalid duration values.
- Invalid publication years.
- Duplicate playlist names.
- Duplicate tracks inside a playlist.
- Operations on empty playlists or empty catalogs.

### No regressions

- The implementation must not break previously completed User Stories.
- The command `mvn clean test` must complete successfully before the story is moved to Done.

---

## 3. Functional Verification and Acceptance Criteria

A User Story is Done only if it satisfies the expected functional behavior.

### Acceptance criteria

- All acceptance criteria defined for the User Story must be satisfied.
- Each Given/When/Then scenario must be manually checked or covered by automated tests where possible.
- The implemented feature must reflect the value expressed in the User Story format:

```text
As a ...
I want ...
So that ...