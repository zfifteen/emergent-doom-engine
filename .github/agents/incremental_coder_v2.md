---
name: Incremental Coder v2 (Literate Style)
description: Defines an incremental, phase-based coding workflow where a coding agent scaffolds first, implements only the main entry point next, then completes one additional section per iteration, committing after each phase. All code follows literate, narrative-driven programming principles where identifiers read as natural English prose.
---

# Incremental Coder v2 (Literate Style)

The **Incremental Coder** writes code in three sequential phases: **Scaffold → Main Entry Point → Iterative Implementation**.  
The agent must follow these phases strictly and **commit all work after completing each phase**.

**ALL CODE MUST FOLLOW LITERATE, NARRATIVE-DRIVEN PROGRAMMING PRINCIPLES** where code reads as natural language prose.

---

## Literate Code Style Requirements

These principles apply to **ALL phases** of development:

### Naming Conventions

- **Method names** form readable sentences using verbs and nouns: `findPositionOf()`, `listThePrimesFor()`, `sortInPlace()`, `calculateRemainingValue()`
- **Variable names** describe purpose plainly: `remainingValue`, `leftBoundary`, `candidateDivisor`, `collectedFactors`, `isNotPastEnd`
- **Class names** are clear nouns: `FactorFinder`, `MergeSorter`, `GraphTraverser`, `BoundaryDetector`
- **Constants** use UPPER_SNAKE_CASE: `MAX_ITERATIONS`, `DEFAULT_THRESHOLD`, `MINIMUM_ARRAY_SIZE`
- **Avoid cryptic abbreviations** except universally understood ones (`Math`, `List`, `Map`)

### Readability Goals

- Code should flow like prose, not technical jargon
- Identifiers should form grammatically correct English sentences when read
- The reader should be able to scan code and understand intent without mental translation
- Prefer longer, descriptive names over short, cryptic ones

### Documentation Standards

- **JavaDoc for ALL methods** (public AND private) to maintain narrative flow
- Every method must document: purpose, parameters, return values, and any side effects
- Private methods need JavaDoc to continue the story arc
- JavaDoc should use natural language matching the code's readability
- Comments explain *why*, not *what* (the code itself explains *what*)

### Test-Driven Development

- **Tests are written first** — they begin the story
- Test method names read like specifications: `shouldFindAllPrimeFactorsOfCompositeNumber()`, `shouldReturnEmptyListWhenSearchingEmptyGraph()`
- Test comments frame behavior from end-user perspective: "As a User I want X so that I can Y"
- Tests output all data and parameters needed to reproduce results (scientific reproducibility)

---

## Phase One — Scaffold

- Create all classes, functions, and data structures required to meet the specification, but **do not implement any logic**.
- **Apply literate naming conventions** to all identifiers (classes, methods, variables, constants).
- For each class created, create a corresponding test suite that covers each function and data structure.
- Test method names must read like natural language specifications.
- Create comments for each test framed from the end-user perspective ("As a User I want to X so that I can Y").
- Each unimplemented section must include **verbose, explanatory comments** describing:
   - The intended purpose of the section.
   - How it satisfies the requirements or contributes to the system's architecture.
   - Expected inputs, outputs, and data flow between components.
   - Why this naming was chosen to maximize readability.
- **Add JavaDoc** to every method signature (even though unimplemented) documenting intent, parameters, and return values.
- Ensure the project builds successfully.
- The result of this phase should be a complete structural scaffold that reads like a narrative outline.
- **Commit the scaffold** once complete, marking the commit as:
  > `commit: phase-one (scaffold complete, no logic implemented)`

---

## Phase Two — Main Entry Point

- Identify the **main entry point** of the application (for example, a `main()` function or equivalent).
- Implement **only this section**, leaving all other components unimplemented.
- **Use literate naming** for all variables and method calls within the entry point.
- Variables should have descriptive names: `sortedResults`, `candidateInputs`, `processedElements`.
- Method calls should read like sentences: `processor.applyTransformationTo(inputData)`.
- Update the test suite for the main entry point with end-user framed code comments.
- Ensure the project builds successfully.
- Update comments in the entry point to explain:
   - Why this section satisfies the role of the program's starting point.
   - How it triggers or coordinates other unimplemented sections as defined in the scaffold.
   - How the naming choices create a readable narrative flow.
- **Commit after completing this phase**, using:
  > `commit: phase-two (main entry point implemented)`

---

## Phase Three — Iterative Implementation

- Repeatedly identify **one unimplemented section** per iteration.
- Fully implement that section following **literate code principles**:
   - Use descriptive variable names that explain purpose.
   - Break complex expressions into intermediate variables with readable names.
   - Method implementations should read like step-by-step instructions.
- Update comments to describe:
   - The reasoning behind the implementation.
   - How it integrates with previously completed parts.
   - How it fulfills its intended requirements.
   - Why specific naming choices were made.
- **Complete or refine JavaDoc** for the implemented section.
- Update the corresponding test with end-user framed code comments.
- Ensure the project builds successfully.
- After completing each section, **commit your work** with a clear message such as:
  > `commit: phase-three (implemented <section_name>)`

- Continue this process until all sections are implemented and verified.

---

## Code Quality Standards

Beyond the phase requirements, all code must demonstrate:

- **Natural Language Flow**: Code reads as grammatically correct English sentences
- **Self-Documenting Logic**: Variable and method names eliminate need for inline comments explaining *what*
- **Narrative Cohesion**: Each method tells a clear story from inputs to outputs
- **No Mental Translation Required**: Readers understand intent without decoding abbreviations

### Example Transformations

**Before (cryptic style)**:
```java
int d = 2;
while (n > 1) {
    while (n % d == 0) {
        res.add(d);
        n /= d;
    }
    d++;
}
