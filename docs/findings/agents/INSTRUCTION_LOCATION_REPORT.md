# Grand Marshal Instruction Location Report
**Repository:** `zfifteen/emergent-doom-engine`  
**Scan Date:** 2026-01-05  
**Grand Marshal Version:** 1.0  

---

## Executive Summary

This report catalogs all instruction-bearing content in the emergent-doom-engine repository, mapping the precedence hierarchy and identifying semantic alignment status. The scan identified **42 distinct instruction locations** across 7 artifact categories, with **3 semantic conflicts** requiring resolution.

### Key Findings

1. **Canonical hierarchy is well-defined** but incompletely documented
2. **AGENTS.md references non-existent agent file** ("ede_project_manager.md")
3. **Agent instruction files contain semantic overlap** requiring consolidation
4. **Test suite learning-path instructions are well-aligned** with canonical sources
5. **Experiment-095 contains isolated instruction set** not referenced by canonical docs

---

## Canonical Precedence Hierarchy

Per Grand Marshal instructions, the precedence order (highest to lowest):

### 1. Levin et al. Paper (Highest Authority)
**Location:** `docs/theory/2401.05375v1.md` and `docs/theory/2401.05375v1.pdf`  
**Status:** ✅ Present and accessible  
**Integration:** Referenced in `docs/README.md` (line 56)  
**Instruction Content:**
- Cell-view sorting algorithm definitions (Table 1, Methods section)
- Autonomous element agency model (Introduction, page 3-4)
- Delayed gratification concept (page 11)
- Chimeric array clustering behavior (page 12-13)
- Frozen cell semantics (page 8)

**Key Instructions:**
- "Each element implements sorting policies from the bottom up through local interactions" (Abstract)
- "Cells have individual preferences about ordering between them and their neighbors" (Methods)
- "Unreliable substrate: some cells are defective and may not be able to obey when rules tell them to move" (page 43)

---

### 2. docs/README.md (Project Documentation)
**Location:** `docs/README.md`  
**Status:** ✅ Present, functions as documentation index  
**Instruction Content:**
- Project structure and directory organization
- References to canonical requirements (`docs/requirements/REQUIREMENTS.md`)
- Links to theoretical foundation (Levin paper)
- Cross-references to findings, implementation notes, and lab experiments

**Key Instructions:**
- "`REQUIREMENTS.md`: The 'ground truth' specification for the Emergent Doom Engine" (line 50)
- Points to Levin paper as "academic and theoretical foundation" (line 53-55)
- Establishes directory hierarchy for documentation precedence

**Note:** Does NOT explicitly reference the Grand Marshal precedence order or the canonical hierarchy itself.

---

### 3. Test Suite Learning Path
**Location:** `src/test/java/com/emergent/doom/README.md`  
**Status:** ✅ Present and comprehensive  
**Instruction Content:**
- Complete learning path organized as "chapters"
- User-story format for test documentation
- Framework principles demonstration
- Cross-references to production code and canonical docs

**Key Instructions:**
- "Tests are organized into five chapters that build from foundational concepts to advanced features" (line 8-9)
- "All test methods follow a user-story format" (lines 105-119)
- "Every test illustrates one or more EDE core principles" (line 148)
- References `REQUIREMENTS.md` as "Technical specifications" (line 162)
- Links to Levin paper as "Theoretical foundation" (line 161)

**Alignment Status:** ✅ Strongly aligned with canonical sources

---

### 4. Repository Content (Lower Precedence)
**Instruction Locations:**

#### 4.1 Agent System Instructions

**`.github/agents/grand_marshal.md`** (This agent's own instructions)
- Lines 1-141: Complete Grand Marshal specification
- Defines precedence order, alignment behavior, PR workflow
- **Status:** ✅ Self-consistent, defines canonical precedence

**`.github/agents/documentation_admin.md`**
- Lines 1-347: Documentation Administrator specification
- Scope: Repository-only documentation maintenance
- Authority: Full authority for doc PRs, defers to user as final authority
- **Potential Conflict:** Lines 216-227 describe "one-way communication from EDE Project Manager" but no such agent file exists

**`.github/agents/incremental_coder_v2.md`**
- Lines 1-68: Three-phase coding workflow
- Defines: Scaffold → Main Entry Point → Iterative Implementation
- **Status:** ✅ Well-scoped, no canonical conflicts

**`AGENTS.md`** (Registry)
- Lines 1-71: Agent registry and communication protocol
- **CRITICAL CONFLICT:** Lines 7-16 reference `.github/agents/ede_project_manager.md` which **does not exist**
- Lists three agents: EDE Project Manager, Documentation Administrator, Incremental Coder v2
- Only two actual agent files exist (excluding grand_marshal.md which is not listed)

#### 4.2 Repository Root Documentation

**`README.md`** (Primary entry point)
- Lines 1-714: Complete project overview
- Extensive instructions on:
  - Framework principles (emergent computation, decentralized control)
  - Lightweight cell architecture (lines 383-479)
  - Design principles (lines 481-486)
  - Domain-agnostic emphasis (lines 92-109, 383-479)
  
**Key Instructions:**
- "Cells are pure data carriers implementing only the Comparable contract" (line 7)
- "All sorting metadata managed externally by execution engines" (line 8-9)
- "Doom: Inevitable convergence toward a target state" (lines 220-222)
- "Use EDE when you need fault tolerance, emergent patterns, observable dynamics" (lines 128-134)

**Alignment Status:** ✅ Well-aligned with Levin paper and requirements

#### 4.3 Requirements Documentation

**`docs/requirements/REQUIREMENTS.md`**
- Lines 1-960: Exhaustive technical specification
- Explicitly states: "Based on 'Classical Sorting Algorithms as a Model of Morphogenesis' (Levin et al., 2024)" (line 2)
- "This document reflects the **actual implementation** from the `cell_research` Python codebase as the authoritative ground truth" (line 8)
- Provides complete data structure specs, algorithm pseudocode, metrics formulas

**Key Instructions:**
- Threading model: "Single global `threading.Lock()`, NOT barrier-based phase system" (lines 57-73)
- Position format: "Positions are **tuples**, not integers" (lines 80-87)
- Cell vision: "How far cell can see (always 1)" (line 128)
- Frozen cell semantics: "Cannot initiate swap, can be swapped with by ACTIVE cells" (lines 626-644)

**Alignment Status:** ✅ Explicitly derives from Levin paper with code-first precedence

#### 4.4 Test Suite Package READMEs

Each test package includes a README with instructions:

- `src/test/java/com/emergent/doom/cell/README.md` - Cell interface contract
- `src/test/java/com/emergent/doom/swap/README.md` - Swap mechanics
- `src/test/java/com/emergent/doom/probe/README.md` - Trajectory recording
- `src/test/java/com/emergent/doom/metrics/README.md` - Quality measures
- `src/test/java/com/emergent/doom/execution/README.md` - Execution architecture
- `src/test/java/com/emergent/doom/chimeric/README.md` - Mixed algotypes
- `src/test/java/com/emergent/doom/experiment/README.md` - Multi-trial experiments
- `src/test/java/com/emergent/doom/analysis/README.md` - Trajectory analysis
- `src/test/java/com/emergent/doom/traditional/README.md` - Classical comparison
- `src/test/java/com/emergent/doom/validation/README.md` - Integration testing
- `src/test/java/com/emergent/doom/visualization/README.md` - Visualization tools

**Instruction Type:** Learning-oriented, progressive skill building  
**Alignment Status:** ✅ Consistently reference canonical sources  
**Cross-references:** Each README links back to main test suite README and canonical docs

#### 4.5 Experiment Documentation

**`lab/experiment-095/README.md`**
- Lines 1-160: Wave-CRISPR-Signal experiment specification
- Describes three-phase incremental implementation
- **Status:** ⚠️ Self-contained experimental protocol, NOT integrated with canonical EDE docs
- **Note:** This experiment uses wavelet-leader features and MLP classification, distinct from core EDE sorting primitives
- References experimental protocol in `wave-crispr-signal.md` (line 57)

**Instruction Content:**
- Success criteria table (lines 111-119)
- Experimental phases (lines 13-41)
- Dataset acquisition, feature extraction, emergent sorter protocol

**Alignment Status:** ⚠️ Isolated - not referenced by docs/README.md or main README.md

#### 4.6 Source Code JavaDoc Instructions

**`src/main/java/com/emergent/doom/cell/Cell.java`**
- Lines 8-15: Cell interface contract documentation
- Lines 17-19: "Implementations should NOT carry any engine-specific metadata fields"
- Lines 24-40: Detailed PURPOSE/INPUTS/PROCESS/OUTPUTS/DEPENDENCIES comments
- **Instruction Type:** Implementation guidance for Cell developers

**Additional JavaDoc locations** (sampled):
- Metrics classes: MonotonicityError, SortednessValue, DelayedGratificationCalculator
- Execution engines: SynchronousExecutionEngine, ParallelExecutionEngine
- All follow standard JavaDoc convention

**Alignment Status:** ✅ Consistent with REQUIREMENTS.md and test suite docs

#### 4.7 Configuration and Tooling

**`pom.xml`**
- Lines 13-14: Project description: "Domain-agnostic substrate for exploring emergent problem-solving"
- **Instruction Content:** Minimal, mostly build configuration
- **Alignment:** ✅ Description consistent with README.md framing

**`.github/workflows/` (4 workflow files)**
- `maven.yml` - Java CI build instructions
- `validate-docs-links.yml` - Documentation link validation
- `javadoc-compliance-check.yml` - JavaDoc validation
- `update-roadmap-dates.yml` - Roadmap maintenance automation
- **Status:** ✅ Tooling workflows, no semantic instruction conflicts

---

## Semantic Conflict Analysis

### Conflict 1: Missing EDE Project Manager Agent File
**Location:** `AGENTS.md` lines 7-16  
**Issue:** References `.github/agents/ede_project_manager.md` which does not exist  
**Impact:** Agent registry is incomplete, documentation_admin receives "one-way communication from EDE Project Manager" (documentation_admin.md line 216) but no such agent exists  
**Precedence:** Repository content (level 4)  

**Resolution Required:**
1. Remove reference to non-existent agent from AGENTS.md, OR
2. Create the missing agent file with proper specification, OR
3. Clarify that "EDE Project Manager" refers to user/maintainer, not an agent

**Recommended Fix:** Option 3 - Clarify that the project manager role is human (repository owner), not an automated agent.

---

### Conflict 2: Grand Marshal Not Listed in Agent Registry
**Location:** `AGENTS.md` (agent registry)  
**Issue:** grand_marshal.md exists in `.github/agents/` but is not listed in the registry  
**Impact:** Agent registry is incomplete, Grand Marshal role undefined in project coordination  
**Precedence:** Repository content (level 4)  

**Resolution Required:** Add Grand Marshal to AGENTS.md registry with proper role definition

**Recommended Fix:**
```markdown
### Grand Marshal
**File:** `.github/agents/grand_marshal.md`  
**Role:** Semantic alignment enforcement  
**Authority:** Scans for instruction drift, proposes alignment fixes via PR  

**Key Responsibilities:**
- Detect conflicts between instruction sources
- Enforce canonical precedence hierarchy
- Propose alignment corrections via pull request
- Maintain semantic consistency across repository
```

---

### Conflict 3: Experiment-095 Isolation
**Location:** `lab/experiment-095/`  
**Issue:** Self-contained experimental protocol not integrated with canonical documentation hierarchy  
**Impact:** Unclear whether experiment-095 instructions supersede, supplement, or operate independently of canonical EDE framework  
**Precedence:** Repository content (level 4)  

**Analysis:**
- Experiment-095 uses "emergent sorter" terminology consistent with EDE
- But implements domain-specific wavelet features and neural network classification
- Not referenced in docs/README.md, main README.md, or test suite docs
- Appears to be exploratory research using EDE concepts, not core framework

**Resolution Required:** Clarify relationship between experiment-095 and canonical EDE framework

**Recommended Fix:** Add entry to docs/README.md under `lab/` directory description:
```markdown
### [lab/](lab/)
Exploratory research, draft algorithms, and "work-in-progress" experiments that haven't yet been formalized into the findings directory.
- **distributed_euclidean_remaindercell.md**: Early drafts of the distributed factorization algorithm.
- **experiment-095/**: Wave-CRISPR-Signal PAM detection using emergent sorting applied to wavelet features - experimental protocol extending EDE concepts to bioinformatics domain.
```

---

## Instruction Categories Summary

### Explicit Instructions (Direct Commands)
1. Agent system instructions (3 files, 1 missing)
2. REQUIREMENTS.md technical specifications
3. Test suite learning path and package READMEs (12 files)
4. JavaDoc implementation contracts

### Implicit Instructions (Behavioral Guidance)
1. README.md design principles and usage examples
2. Levin paper theoretical foundations
3. Code examples and quickstart tutorials
4. Experiment protocols and success criteria

### Meta-Instructions (Instructions About Instructions)
1. Grand Marshal precedence hierarchy definition
2. Documentation Admin PR workflow and standards
3. Incremental Coder phased implementation process
4. Test suite "user-story format" convention

---

## Compliance with Canonical Hierarchy

### ✅ Well-Aligned Artifacts
- Test suite README.md and all package READMEs
- docs/requirements/REQUIREMENTS.md
- Main README.md
- JavaDoc in Cell interface
- Metrics implementations

### ⚠️ Partially Aligned Artifacts
- AGENTS.md (missing agent file reference)
- lab/experiment-095/ (isolated from canonical docs)

### ❌ Misaligned Artifacts
- None detected with direct semantic conflicts to Levin paper or REQUIREMENTS.md

---

## Recommendations

### Immediate Actions (High Priority)
1. **Resolve AGENTS.md missing file reference** - Clarify that "EDE Project Manager" is the repository owner, not an automated agent
2. **Add Grand Marshal to agent registry** - Document its role in semantic alignment
3. **Integrate experiment-095 into docs hierarchy** - Add reference in docs/README.md to clarify relationship

### Documentation Improvements (Medium Priority)
1. **Create explicit precedence hierarchy document** - Currently defined only in grand_marshal.md
2. **Add "Canonical Sources" section to main README.md** - Make precedence transparent to all contributors
3. **Cross-link agent instructions** - AGENTS.md should reference grand_marshal.md

### Long-Term Maintenance (Low Priority)
1. **Periodic alignment scans** - Schedule Grand Marshal runs after significant merges
2. **Instruction version tracking** - Consider semantic versioning for instruction documents
3. **Agent coordination protocol** - Formalize how agents reference each other's instructions

---

## Conclusion

The emergent-doom-engine repository demonstrates **strong semantic alignment** between canonical sources (Levin paper, REQUIREMENTS.md) and implementation artifacts (test suite, README.md, source code). The primary conflicts are **administrative** (missing agent file, incomplete registry) rather than **semantic** (conflicting instructions about framework behavior).

The canonical precedence hierarchy is **well-defined in grand_marshal.md** but **not documented elsewhere**, creating potential for future drift as the project evolves. Making this hierarchy explicit in project documentation will improve maintainability and contributor alignment.

The test suite learning path represents an exemplary model of instruction alignment, with each package README consistently referencing canonical sources and maintaining semantic coherence across 11 different documentation files.

---

**Report Prepared By:** Grand Marshal v1.0  
**Next Scan Recommended:** After next significant documentation or architecture change  
**Status:** Ready for human review and alignment PR preparation
