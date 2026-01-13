---
name: literate-writer
description: >
  Guide coding agents to write code, tests, and documentation as a single
  coherent narrative in clear natural language prose, with identifiers
  that read as grammatically correct English.
license: MIT
compatibility: opencode/skills
metadata:
  domain: "literate-programming"
  style: "ede-chop-shop"
  languages: "java,python,typescript"
  emphasis: "readability-over-cleverness"
---

# Literate Writer

This skill trains coding agents to treat all code as a readable story rather than a bag of symbols.
When active, it reshapes naming, tests, comments, and structure so a human can skim the codebase as if reading plain English prose.

## When to use this skill

- The user asks for “literate”, “narrative”, or “prose-like” code, or references the EDE Chop Shop style.
- The task involves designing APIs, algorithms, or refactors where human readability is a primary concern.
- The code will be read or maintained by researchers, students, or collaborators who benefit from self-explanatory naming and structure.

## Core principles to apply

1. **Code as narrative**

    - Treat every file as a chapter of a story about behavior and intent, not just mechanics.
    - Prefer straightforward control flow and explicit logic over clever tricks that obscure the narrative.

2. **Identifiers as sentences**

    - Choose method names that read like natural language actions at the call site, such as `findPositionOf`, `listThePrimesFor`, `sortInPlace`.
    - Use descriptive variable names like `remainingValue`, `leftBoundary`, `candidateDivisor` that make loops and conditionals read as prose.
    - Name classes as clear nouns such as `FactorFinder`, `MergeSorter`, `GraphTraverser`; constants use UPPER_SNAKE_CASE like `MAX_ITERATIONS`, `DEFAULT_THRESHOLD`.
    - Avoid cryptic abbreviations except those that are universally recognized (e.g., `URL`, `ID`).

3. **Tests as the opening chapter**

    - Begin by writing tests that describe behavior in natural language: method names like `shouldFindAllPrimeFactorsOfCompositeNumber` or `shouldReturnEmptyListWhenSearchingEmptyGraph`.
    - Let tests serve as both specification and narrative outline; implementation then “fills in” the story so all tests read as true statements.

4. **Documentation as connective tissue**

    - Provide JavaDoc or equivalent docstrings for **all** methods, public and private, explaining purpose, parameters, return values, and side effects in clear prose.
    - Keep documentation concise but complete, ensuring a reader can reconstruct intent from the docs plus the identifiers without external context.

5. **Quality guardrails**

    - Guard aggressively against mathematical errors, logical flaws, and unnecessary inefficiency while preserving readability.
    - Reject code that “reads like gibberish”; prefer slightly more verbose, explicit constructs when they improve clarity for future readers.

## How an agent should behave with this skill

When this skill is active, the agent should:

- Start by sketching tests or usage examples that read like natural language specifications, then design APIs and implementations to satisfy them.
- Consistently favor names and structures that form readable sentences at call sites and within control flow blocks.
- Add or refine documentation to maintain a smooth narrative through the codebase, aligning text and identifiers.
- During review or refactoring tasks, point out places where the story is unclear and propose renamings or restructurings that restore narrative flow.
