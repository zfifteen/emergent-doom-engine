I've completed a comprehensive canonical document sync. Here's my analysis and proposed patches:

***

## **CANONICAL DOCUMENT SYNC REPORT**
**Date:** January 3, 2026  
**Scope:** EDE_PM_INSTRUCTIONS.md, REQUIREMENTS.md vs. newer experimental documents

***

## **FINDINGS SUMMARY**

### ✅ **No Critical Conflicts Found**
Core terminology remains consistent. No definitions contradict each other.

### ⚠️ **3 Terminology Drifts Detected**
Need clarification to prevent future confusion.

### 📋 **4 Implicit Requirement Changes**
New abstractions introduced in experimental docs that should be canonicalized.

***

## **1. TERMINOLOGY DRIFT**

### **Issue 1.1: "Algotype" vs "Strategy"**

**Observed in:** CLUSTERING_PRIMITIVE_SPEC.md, FIRST_NON_SORTING_EXPERIMENT.md

**Current state:**
- REQUIREMENTS.md uses "algotype" exclusively (sorting-specific)
- CLUSTERING_PRIMITIVE_SPEC.md introduces "Strategy" as general term
- FIRST_NON_SORTING_EXPERIMENT.md uses "FactorStrategy" enum

**Analysis:**
This is **intentional evolution**, not drift. The project is correctly generalizing "algotype" (sorting behavior) to "Strategy" (any problem-solving approach). However, canonical docs don't reflect this hierarchy.

**Recommended action:** Update EDE_PM_INSTRUCTIONS.md to establish terminology hierarchy.

***

### **Issue 1.2: "Goal Function" Abstraction**

**Observed in:** CLUSTERING_PRIMITIVE_SPEC.md Section 4.2, FIRST_NON_SORTING_EXPERIMENT.md Section 3.3

**Current state:**
- REQUIREMENTS.md assumes "sorted" is the only goal
- Experimental docs introduce "GoalFunction<T>" interface and "factor fitness" as alternative goal

**Analysis:**
This is a **necessary abstraction** to support non-sorting applications. Not documented in canonical requirements.

**Recommended action:** Add "Goal Function" as core concept to REQUIREMENTS.md.

***

### **Issue 1.3: "Free Compute" / "Computational Primitive"**

**Observed in:** CLUSTERING_PRIMITIVE_SPEC.md Section 1, EDE_PM_INSTRUCTIONS.md Section 7

**Current state:**
- EDE_PM_INSTRUCTIONS.md Section 7 repeatedly mentions "free compute" but doesn't define it
- CLUSTERING_PRIMITIVE_SPEC.md provides first formal definition
- REQUIREMENTS.md doesn't mention this concept at all

**Analysis:**
"Free compute" is a **central thesis** of the EDE project but lacks canonical definition.

**Recommended action:** Add glossary entry to both canonical docs.

***

## **2. IMPLICIT REQUIREMENT CHANGES**

### **Change 2.1: Strategy Interface Pattern**

**Evidence:** CLUSTERING_PRIMITIVE_SPEC.md Section 4.1

```java
public interface Strategy {
    String getName();
}
```

**Impact:** This pattern isn't in REQUIREMENTS.md, which uses enum-based algotypes.

**Recommendation:** Add to REQUIREMENTS.md Section 2.1 as evolution of algotype concept.

***

### **Change 2.2: ClusteringPrimitive<T> Interface**

**Evidence:** CLUSTERING_PRIMITIVE_SPEC.md Section 4.2

**Impact:** Introduces contract for extracting clustering as reusable component. Not in REQUIREMENTS.md.

**Recommendation:** Add new section to REQUIREMENTS.md: "Section 11: Clustering as Primitive API"

***

### **Change 2.3: GoalFunction<T> Abstraction**

**Evidence:** Multiple references in experimental docs

**Impact:** Enables non-sorting goals. REQUIREMENTS.md assumes sorting only.

**Recommendation:** Refactor REQUIREMENTS.md Section 3 to define abstract goal concept before introducing sorting-specific implementations.

***

### **Change 2.4: Factor Fitness Scoring**

**Evidence:** FIRST_NON_SORTING_EXPERIMENT.md Section 3.3

```java
public double factorFitness(int candidate, int N) {
    int remainder = N % candidate;
    if (remainder == 0) return 1.0;
    int distanceToMultiple = Math.min(remainder, candidate - remainder);
    return 1.0 - ((double) distanceToMultiple / candidate);
}
```

**Impact:** First concrete non-sorting goal function. Establishes pattern for future domains.

**Recommendation:** Add example to REQUIREMENTS.md showing goal function pattern.

***

## **3. PROPOSED PATCHES**

### **PATCH 3.1: EDE_PM_INSTRUCTIONS.md**

**Location:** After Section 3.2 (Bottom-up, emergent mindset)

**Add new subsection:**

```markdown
### 3.2.1 Terminology Hierarchy

The EDE uses the following terminology hierarchy:

- **Strategy**: General term for any problem-solving approach or behavioral policy
  - Example domains: sorting algotypes, factor-finding methods, routing algorithms
- **Algotype**: Sorting-specific instance of Strategy
  - Bubble, Insertion, Selection (original Levin paper context)
- **Goal Function**: Domain-specific success criteria
  - Sorting: monotonic order
  - Factorization: factor-fitness score
  - General: any computable objective function

When discussing non-sorting applications, use "Strategy" and "Goal Function."
When discussing sorting experiments, "algotype" remains the preferred term for consistency with Levin et al.
```

***

### **PATCH 3.2: EDE_PM_INSTRUCTIONS.md Glossary**

**Location:** After Section 10, before final separator

**Add new section:**

```markdown
## 11. Glossary of Core Terms

**Aggregation Value**: Percentage of cells with at least one same-strategy neighbor. Measures spatial clustering.

**Algotype**: Sorting-specific strategy (Bubble, Insertion, Selection). See also: Strategy.

**Clustering/Aggregation**: Emergent grouping of cells with similar strategies during problem-solving.

**Delayed Gratification (DG)**: Temporary decrease in progress toward goal to navigate obstacles.

**Doom**: Inevitability toward a target state (not catastrophe). The inexorable grinding toward solution.

**Free Compute**: Computation performed by emergent dynamics without explicit programming. Example: clustering partitions problem space "for free" during sorting.

**Goal Function**: Domain-specific definition of success. Sorting uses monotonic order; factorization uses factor-fitness; general pattern allows any computable objective.

**Strategy**: General term for problem-solving approach. Algotypes are sorting-specific strategies.

**Unreliable Substrate**: Computational environment with failures (frozen cells, partial execution).
```

***

### **PATCH 3.3: REQUIREMENTS.md**

**Location:** New Section 2.0, insert before current Section 2

**Add:**

```markdown
## 2. Domain Abstraction Layer

### 2.1 Strategy Pattern

To support non-sorting applications, the emergence engine abstracts sorting-specific "algotypes" into general "strategies."

**Strategy Interface:**
```java
public interface Strategy {
    String getName();
    // Extensible: add domain-specific methods as needed
}
```

**Sorting Implementation:**
```java
public enum SortingAlgotype implements Strategy {
    BUBBLE, INSERTION, SELECTION;
    
    @Override
    public String getName() { return this.name(); }
}
```

**Non-Sorting Example:**
```java
public enum FactorStrategy implements Strategy {
    SMALL_PRIMES, FERMAT_NEAR_SQRT, RANDOM_SAMPLE;
    
    @Override
    public String getName() { return this.name(); }
}
```

### 2.2 Goal Function Pattern

Each domain defines success criteria via a goal function:

```java
public interface GoalFunction<T> {
    /**
     * Evaluate how "sorted" / "solved" the array is.
     * @return Value in [0.0, 1.0] where 1.0 = goal achieved
     */
    double evaluate(List<T> items);
    
    /**
     * Compare two items for ordering.
     * @return negative if a < b, positive if a > b, 0 if equal
     */
    int compare(T a, T b);
}
```

**Sorting Implementation:** Sortedness percentage (Section 4.2)

**Factorization Implementation:** Factor-fitness scoring (see experimental docs)

---

**Note:** Sections 2.1 (Cell Class) and 2.2 (Probe Class) from original document are now renumbered to 3.1 and 3.2.
```

***

### **PATCH 3.4: REQUIREMENTS.md**

**Location:** New Section 11, append after Section 10

**Add:**

```markdown
## 11. Clustering as Computational Primitive

### 11.1 Purpose

Clustering is not a side effect—it is "free compute" that partitions problem spaces and reveals structure.

### 11.2 ClusteringPrimitive Interface

```java
public interface ClusteringPrimitive<T> {
    ClusteringResult<T> computePartition(
        List<T> items,
        Map<T, Strategy> strategies,
        GoalFunction<T> goalFunction
    );
    
    List<Integer> extractBoundaries(ClusteringResult<T> result);
    
    Map<IntRange, Strategy> getRegionalDominance(ClusteringResult<T> result);
}
```

### 11.3 Expected Behavior

- **Input:** Array of items with strategy assignments and goal function
- **Process:** Run emergence dynamics (cell-view sorting or equivalent)
- **Output:** Partitioning where clusters represent strategy-compatible regions

### 11.4 Validation Requirements

Before using clustering as primitive:
- [ ] Reproduce paper baseline (72% peak for Bubble-Selection)
- [ ] Control experiments show ~50-61% (random baseline)
- [ ] Cluster boundaries stable across runs (low variance)
- [ ] Information content exceeds random partitioning

### 11.5 Reference Implementation

See `docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md` for detailed API design.
See `docs/requirements/FIRST_NON_SORTING_EXPERIMENT.md` for first non-sorting application.
```

***

## **4. ALIGNMENT VERIFICATION**

### **Goals from EDE_PM_INSTRUCTIONS.md Section 8:**

1. ✅ **Define and validate EDE as general framework** - Experimental docs advance this
2. ✅ **Reproduce Levin-style experiments** - REQUIREMENTS.md covers this
3. ✅ **Turn clustering into practical primitive** - CLUSTERING_PRIMITIVE_SPEC.md addresses this
4. ✅ **Demonstrate non-sorting application** - FIRST_NON_SORTING_EXPERIMENT.md addresses this

**Verdict:** Experimental documents are **correctly aligned** with high-level goals. Patches formalize implicit evolution.

***

## **5. RECOMMENDED ACTIONS**

### **Immediate (High Priority):**
1. Apply PATCH 3.1 to EDE_PM_INSTRUCTIONS.md (terminology hierarchy)
2. Apply PATCH 3.2 to EDE_PM_INSTRUCTIONS.md (glossary)

### **Before Next Experiment (Medium Priority):**
3. Apply PATCH 3.3 to REQUIREMENTS.md (domain abstraction)
4. Apply PATCH 3.4 to REQUIREMENTS.md (clustering primitive)

### **Documentation Hygiene (Low Priority):**
5. Update README.md to reference new sections in canonical docs
6. Cross-link experimental docs to patched canonical sections

***

## **6. CURRENT PROJECT STATUS**

**Current high-level goal:** Validate EDE as domain-general framework

**Current working sub-goal:** Design first non-sorting experiment (factorization)

**You are here:** Experimental designs complete, awaiting implementation of factorization experiment

**Next 3 concrete steps:**
1. **Apply canonical patches** (this report). **Goal this supports:** Maintain alignment between experimental evolution and canonical authority
2. **Implement FactorStrategy and FactorFitnessGoal** per FIRST_NON_SORTING_EXPERIMENT.md. **Goal this supports:** Demonstrate non-sorting application
3. **Run 100-trial factorization experiment** and analyze clustering behavior. **Goal this supports:** Validate clustering as computational primitive

***

**End of Canonical Document Sync Report**

Sources
[1] FIRST_NON_SORTING_EXPERIMENT.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/3449adbc-6453-4998-8b4a-e16b5e94d23d/FIRST_NON_SORTING_EXPERIMENT.md
[2] CLUSTERING_PRIMITIVE_SPEC.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/22997274-8c13-4aaf-b323-83e95a1f261e/CLUSTERING_PRIMITIVE_SPEC.md
[3] README.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/063a5f1c-af7c-4ea1-96ea-3a2c8a8e6a06/README.md
[4] EDE_PM_INSTRUCTIONS.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/5b3a4efb-5529-47cc-bf2e-c3a16c4208d2/EDE_PM_INSTRUCTIONS.md
[5] REQUIREMENTS.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/49eb67d2-0378-4dc1-9c0a-a5198863cb6b/REQUIREMENTS.md
[6] 2401.05375v1.md https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/671d9f96-9511-4b39-959e-125dd45a898b/2401.05375v1.md
[7] 2401.05375v1.pdf https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/collection_cd8c922a-4868-4f41-a92c-d8f6594b8b05/06ac4479-578c-40d8-a19e-ac391004090b/2401.05375v1.pdf
