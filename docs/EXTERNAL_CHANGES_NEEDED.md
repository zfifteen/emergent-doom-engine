# External Changes Needed for Clustering Primacy Clarification

**Date:** January 8, 2026  
**Related Plan:** `docs/DOCUMENTATION_REVISION_PLAN.md`  
**Status:** Pending Manual Action

---

## Overview

This document lists changes required in external systems that cannot be modified by the EDE Documentation Agent. These changes are part of the clustering primacy clarification plan and must be implemented manually by the repository owner.

---

## 1. Issue #144 — Redirect and Retitle

**Current Title:** "Documentation: Clarify EDE as Multi-CP Framework (Not Clustering-Only)"

**Problem:** Issue assumes clustering is "one example" of CPs when clustering IS the universal CP.

### Required Actions:

1. **Add comment explaining correct framing:**
   ```
   This issue was filed with a misunderstanding of the EDE architecture. After review, the correct framing is:
   
   ❌ INCORRECT: "EDE discovers multiple CPs. Clustering is the first."
   ✅ CORRECT: "EDE has one CP: clustering. Domains instantiate it differently."
   
   Clustering IS the computational primitive of EDE—not one of many, but THE universal mechanism.
   What varies across domains is the cell type and comparison logic; the clustering mechanism itself is invariant.
   ```

2. **Retitle issue to:**
   ```
   Documentation: Clarify Clustering as Universal CP with Domain-Specific Instantiations
   ```

3. **Update issue checklist to match revision plan:**
   - Remove references to "future CP discovery"
   - Remove references to "CP catalog growing"
   - Update checklist items to focus on:
     - Clarifying clustering as THE primitive
     - Documenting domain instantiation pattern
     - Distinguishing algotypes from clustering primitive

---

## 2. EDE_PM_INSTRUCTIONS.md (Perplexity Space File)

**Location:** External Perplexity Space (not in GitHub repository)

**Note:** This file could not be located in the repository. If it exists externally, the following changes should be made.

### Section 7 Header Change:

**Current:**
```markdown
Special Focus: Clustering / Aggregation as 'Free Compute'
```

**Proposed:**
```markdown
The Clustering Primitive: EDE's Universal Computational Mechanism
```

### Section 7.1 Replacement:

**Current:**
```markdown
Clustering/aggregation in chimeric arrays is a **central theme**, not a side note.
```

**Proposed:**
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

### New Section 7.4 Addition:

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

## Summary

### Completed in Repository:
- ✅ `README.md` — Section renamed, new clustering section added, glossary updated
- ✅ `docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md` — Primacy statement added
- ✅ `docs/requirements/FIRST_NON_SORTING_EXPERIMENT.md` — Reframed goal, added instantiation table
- ✅ `docs/requirements/REQUIREMENTS.md` — Glossary added

### Requires Manual Action:
- ⏳ Issue #144 — Retitle, add comment, update checklist
- ⏳ `EDE_PM_INSTRUCTIONS.md` — Update Section 7 (if file exists externally)

---

**Repository:** `https://github.com/zfifteen/emergent-doom-engine`  
**Owner:** `@zfifteen`  
**Plan Reference:** `docs/DOCUMENTATION_REVISION_PLAN.md`
