# Documentation Revision Plan: Clustering Primacy Clarification

**Date:** January 8, 2026  
**Author:** PM Agent (via Perplexity Space)  
**Status:** Proposed  
**Related Issue:** #144 (to be redirected, not implemented as originally written)

---

## Executive Summary

This plan addresses terminology clarity across EDE documentation. The core insight:

> **Clustering IS the computational primitive of EDE.** What varies across domains is what gets clustered (cell types) and how cells compare (comparison logic). The clustering mechanism itself is universal and invariant.

Issue #144 was filed with a misunderstanding—proposing to downgrade clustering to "one of many CPs." This plan instead **strengthens clustering's position as THE primitive** while clarifying how it instantiates across domains.

---

## Architectural Truth (Canonical Reference)

```
EDE Framework
├── Morphogenetic Cell Dynamics (mechanism)
│   ├── Local pairwise comparisons
│   ├── Swap-based position updates  
│   └── Algotypes travel WITH cells
│
└── CLUSTERING (THE Computational Primitive)
    ├── Emerges from cell dynamics
    ├── Universal across all domains
    └── Instantiated differently per domain:
        │
        ├── Sorting Domain
        │   ├── Cell type: IntegerCell
        │   ├── Comparison: cell.value (integer ordering)
        │   └── What clusters: BUBBLE vs INSERTION vs SELECTION algotypes
        │
        ├── Factorization Domain  
        │   ├── Cell type: FactorCell
        │   ├── Comparison: factor fitness score
        │   └── What clusters: SMALL_PRIMES vs FERMAT vs RANDOM strategies
        │
        └── [Future Domain]
            ├── Cell type: [DomainCell implements Comparable]
            ├── Comparison: [domain-specific ordering]
            └── What clusters: [domain-specific strategies]
```

**Key Distinction:**
- **Algotypes/Strategies** = behavioral policies assigned to cells (INPUT to system)
- **Clustering** = emergent spatial segregation by strategy (OUTPUT from system)
- **Delayed gratification, error tolerance** = properties of clustering under constraints (not separate primitives)

---

## Document-by-Document Revision Plan

### 1. Issue #144 — Redirect (Do Not Implement As Written)

**Current Title:** "Documentation: Clarify EDE as Multi-CP Framework (Not Clustering-Only)"

**Problem:** Issue assumes clustering is "one example" of CPs when clustering IS the universal CP.

**Action:** 
- [ ] Add comment explaining the correct framing
- [ ] Retitle to: "Documentation: Clarify Clustering as Universal CP with Domain-Specific Instantiations"
- [ ] Update checklist to match this revision plan
- [ ] Remove references to "future CP discovery" and "CP catalog growing"

**Replacement Framing:**
```
❌ INCORRECT: "EDE discovers multiple CPs. Clustering is the first."
✅ CORRECT: "EDE has one CP: clustering. Domains instantiate it differently."
```

---

### 2. GitHub README.md — Section Rename + Clarification

**File:** `README.md` (repository root)

**Current Issue:** Section titled "Computational Primitives" lists algotypes (BUBBLE, INSERTION, etc.)

**Revision:**

| Current | Proposed |
|---------|----------|
| `## Computational Primitives` | `## Algotype Implementations` |
| Lists BUBBLE, INSERTION, SELECTION, FIBONACCI | Same content, but framed as "behavioral policies" |

**Add New Section** (after Algotype Implementations):

```markdown
## The Clustering Primitive

Clustering is THE computational primitive of the Emergent Doom Engine. When cells with different algotypes (behavioral strategies) interact through local comparisons and swaps, they spontaneously segregate into spatial clusters by strategy type.

**What varies across domains:**
- **Cell type**: What entity is being sorted/organized
- **Comparison logic**: How cells determine ordering (`compareTo()`)
- **Strategy assignments**: Which algotypes/policies are mixed

**What remains constant:**
- The clustering mechanism (spatial segregation by strategy)
- The aggregation metric (% of cells with same-type neighbors)
- The emergent properties (delayed gratification, error tolerance)

For the formal specification, see [CLUSTERING_PRIMITIVE_SPEC.md](docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md).
```

---

### 3. EDE_PM_INSTRUCTIONS.md (Space File) — Strengthen Section 7

**File:** `EDE_PM_INSTRUCTIONS.md` (Perplexity Space)

**Current Text (Section 7 Header):**
> "Special Focus: Clustering / Aggregation as 'Free Compute'"

**Proposed Header:**
> "The Clustering Primitive: EDE's Universal Computational Mechanism"

**Current Text (Section 7.1):**
> "Clustering/aggregation in chimeric arrays is a **central theme**, not a side note."

**Proposed Replacement:**
```markdown
## 7. The Clustering Primitive

Clustering is THE computational primitive of EDE—not one of many, but THE universal mechanism that powers all EDE applications.

### 7.1 Primacy of Clustering

Clustering/aggregation is not merely a "central theme"—it is the **singular computational primitive** that the Emergent Doom Engine extracts from morphogenetic dynamics. Every EDE application, regardless of domain, uses the same clustering mechanism:

1. Cells with different strategies (algotypes) are mixed in an array
2. Local pairwise comparisons drive swap decisions
3. Strategies spontaneously segregate into spatial clusters
4. The clustering signal reveals structure in the problem space

**What varies:** Cell types, comparison logic, strategy definitions
**What is constant:** The clustering mechanism itself
```

**Add to Section 7 (new subsection):**

```markdown
### 7.4 Domain Instantiation Pattern

When extending EDE to a new domain, the PM should ensure:

1. **Cell type defined**: `class DomainCell implements Comparable<DomainCell>`
2. **Comparison logic specified**: What does `compareTo()` mean in this domain?
3. **Strategies enumerated**: What are the algotypes/behavioral policies?
4. **Success metric mapped**: What does "sorted" mean? (clustering happens relative to this goal)

The clustering primitive remains unchanged. Only its instantiation parameters vary.

**Template Questions for New Domains:**
- "What entities will be the cells?"
- "What ordering relationship drives swaps?"
- "What strategies will compete for spatial dominance?"
- "How will we measure clustering success?"
```

---

### 4. CLUSTERING_PRIMITIVE_SPEC.md (Space File) — Add Primacy Statement

**File:** `CLUSTERING_PRIMITIVE_SPEC.md` (Perplexity Space)

**Current Executive Summary:**
> "This document formalizes how the emergent clustering behavior observed in chimeric sorting arrays can be extracted and reused as a **general-purpose computational primitive**."

**Proposed Addition (prepend to Executive Summary):**

```markdown
## 1. Executive Summary

**Clustering is THE computational primitive of the Emergent Doom Engine.**

This document formalizes the universal clustering mechanism that powers all EDE applications. Clustering is not one of several primitives—it is the singular emergent pattern that the framework extracts from morphogenetic cell dynamics. Different domains instantiate clustering with different cell types and comparison logic, but the primitive itself is invariant.

[rest of existing content...]
```

**Update Section 5 Header:**

| Current | Proposed |
|---------|----------|
| `## 5. Non-Sorting Applications` | `## 5. Domain Instantiations` |

**Add clarifying text to Section 5:**

```markdown
## 5. Domain Instantiations

The following examples show how the SAME clustering primitive instantiates across different domains. Each domain provides:
- A cell type (the entity being clustered)
- A comparison function (the ordering that drives swaps)
- Strategy definitions (the algotypes that will segregate)

The clustering mechanism is identical in all cases.
```

---

### 5. FIRST_NON_SORTING_EXPERIMENT.md (Space File) — Minor Reframe

**File:** `FIRST_NON_SORTING_EXPERIMENT.md` (Perplexity Space)

**Current Goal Statement:**
> "Demonstrate EDE on a non-sorting application using clustering as computational primitive"

**Proposed:**
> "Demonstrate clustering primitive instantiation for integer factorization domain"

**Current Hypothesis:**
> "When cells encode factor candidates and algotypes encode factor-finding strategies, emergent clustering will group candidates..."

**Assessment:** ✅ This is already correctly framed. No changes needed to hypothesis.

**Add to Section 10 (Links to Goals):**

```markdown
| This experiment instantiates | The universal clustering primitive |
|------------------------------|-----------------------------------|
| Cell type | FactorCell (candidate integer) |
| Comparison logic | Factor fitness score |
| Strategies | SMALL_PRIMES, FERMAT_NEAR_SQRT, RANDOM_SAMPLE |
```

---

### 6. REQUIREMENTS.md (Space File) — Add Glossary Entries

**File:** `REQUIREMENTS.md` (Perplexity Space)

**Add New Section at End:**

```markdown
## Appendix: Glossary

### Clustering Primitive
The singular computational primitive of EDE. Emergent spatial segregation of cells by strategy/algotype. Universal across all domains; instantiated with domain-specific cell types and comparison logic.

### Algotype
A behavioral policy assigned to a cell determining its swap decision logic. Examples: BUBBLE, INSERTION, SELECTION (sorting domain); SMALL_PRIMES, FERMAT (factorization domain). Algotypes are INPUTS to the system; clustering is the OUTPUT.

### Domain Instantiation
The process of applying the clustering primitive to a new problem domain by specifying: (1) cell type, (2) comparison logic, (3) strategy enum.

### TBPC
Two-Basin Problem-Capacity. A factorization-domain construct describing how problem instances partition into (at least) two basins of attraction with respect to algorithm performance or capacity.
```

---

### 7. GitHub Repository README.md — Glossary Alignment

**File:** `README.md` (repository root)

**Current Glossary Entry:**
> **Doom**: Inevitable convergence toward a target state...

**Add Entry:**
```markdown
**Clustering Primitive**: The singular computational mechanism of EDE. Emergent spatial segregation by strategy type, universal across all domains. What varies per domain is the cell type and comparison logic; the clustering mechanism is invariant.
```

**Verify/Update Entry:**
```markdown
**Algotype**: Behavioral policy determining swap decisions—BUBBLE, INSERTION, SELECTION, FIBONACCI for sorting; domain-specific strategies for other applications. Algotypes are inputs; clustering is the emergent output.
```

---

## Implementation Order

| Priority | Document | Change Type | Effort |
|----------|----------|-------------|--------|
| 1 | Issue #144 | Redirect/retitle | 10 min |
| 2 | EDE_PM_INSTRUCTIONS.md | Section 7 rewrite | 30 min |
| 3 | CLUSTERING_PRIMITIVE_SPEC.md | Add primacy statement | 15 min |
| 4 | GitHub README.md | Section rename + new section | 20 min |
| 5 | REQUIREMENTS.md | Add glossary | 15 min |
| 6 | FIRST_NON_SORTING_EXPERIMENT.md | Minor reframe | 10 min |

**Total Estimated Effort:** ~2 hours

---

## Success Criteria

After implementing this plan:

- [ ] No document implies clustering is "one of several" primitives
- [ ] Clustering is consistently described as THE universal primitive
- [ ] Algotypes are clearly distinguished from the clustering primitive
- [ ] Domain instantiation pattern is documented with template questions
- [ ] Issue #144 reflects correct architectural understanding
- [ ] TBPC acronym is defined (pending user input)

---

## Validation Questions

Before finalizing, confirm with user:

1. Is the "Domain Instantiation Pattern" template useful for guiding future experiments?
2. Should delayed gratification and error tolerance be explicitly documented as "properties of clustering under constraints" rather than separate concepts?
3. What does TBPC stand for? (Needed for glossary)

---

## References

- [CLUSTERING_PRIMITIVE_SPEC.md](docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md) — Formal specification
- [FIRST_NON_SORTING_EXPERIMENT.md](FIRST_NON_SORTING_EXPERIMENT.md) — Factorization instantiation
- [Levin et al. (2024)](docs/theory/2401.05375v1.pdf) — Theoretical foundation
- [Issue #144](https://github.com/zfifteen/emergent-doom-engine/issues/144) — Original (misframed) issue
