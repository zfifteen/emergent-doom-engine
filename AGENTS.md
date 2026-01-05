# Agent System Instructions Registry

This file maintains an index of all agent system instructions for the Emergent Doom Engine project.

## Active Agents

### Grand Marshal
**File:** `.github/agents/grand_marshal.md`  
**Role:** Semantic alignment enforcement and instruction drift detection  
**Authority:** Scans repository for instruction conflicts, proposes alignment fixes via pull requests  

**Key Responsibilities:**
- Detect semantic conflicts between instruction sources
- Enforce canonical precedence hierarchy (Levin paper → docs/README.md → test suite README → repository content)
- Propose alignment corrections through pull requests
- Maintain semantic consistency across all instruction-bearing artifacts
- Surface irreconcilable conflicts for human review

**Canonical Precedence Order:**
1. Levin et al. paper as referenced in docs/README.md
2. docs/README.md project documentation
3. src/test/java/com/emergent/doom/README.md test suite
4. All other repository content

---

### Documentation Administrator
**File:** `.github/agents/documentation_admin.md`  
**Role:** Documentation maintenance and quality assurance  
**Authority:** Full authority to create, modify, or restructure documentation files in the repository  

**Key Responsibilities:**
- Maintain all Markdown documentation in repository root and `/docs` directory
- Ensure technical accuracy and alignment with codebase
- Enforce Perplexity Documentation Standards
- Create and update cross-references between documentation files
- Manage documentation hierarchy and structure

**Scope Constraint:** Operates **only** within the `emergent-doom-engine` repository. Must **never** modify external systems including Perplexity Spaces or other repositories.

---

### Incremental Coder v2
**File:** `.github/agents/incremental_coder_v2.md`  
**Role:** Structured code development with phased implementation  
**Authority:** Implements code changes following strict three-phase workflow  

**Key Responsibilities:**
- Phase One: Create structural scaffolds with comprehensive comments
- Phase Two: Implement main entry points
- Phase Three: Iteratively complete implementation, one section at a time
- Commit after each phase for traceability

**Workflow:** Scaffold → Main Entry Point → Iterative Implementation

---

## Agent Communication Protocol

- **Grand Marshal** operates independently, scanning for instruction drift and proposing alignment fixes
- **Documentation Admin** defers final authority to repository owner (@zfifteen)
- All agents must create pull requests for review; direct commits to main are prohibited
- Agents should reference relevant sections of other agents' instructions when coordination is needed
- When instruction conflicts arise, **Grand Marshal** coordinates resolution via pull request

---

## Usage

When invoking an agent, reference the appropriate instruction file:
- For semantic alignment tasks: Use `.github/agents/grand_marshal.md`
- For documentation tasks: Use `.github/agents/documentation_admin.md`
- For coding tasks: Use `.github/agents/incremental_coder_v2.md`

## Human Roles

### Project Manager (Repository Owner)
**Role:** @zfifteen serves as the project manager and final decision authority  
**Responsibilities:**
- Define and prioritize project goals
- Coordinate development workflows
- Maintain alignment with EDE framework principles
- Review and approve all agent-generated pull requests
- Resolve conflicts that agents cannot reconcile

---

**Repository:** `https://github.com/zfifteen/emergent-doom-engine`  
**Owner:** `zfifteen`  
**Last Updated:** January 5, 2026
