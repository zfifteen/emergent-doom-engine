# Agent System Instructions Registry

This file maintains an index of all agent system instructions for the Emergent Doom Engine project.

## Active Agents

### EDE Project Manager
**File:** `.github/agents/ede_project_manager.md`  
**Role:** Strategic oversight and project coordination  
**Authority:** Provides guidance to other agents, maintains alignment with project goals, coordinates multi-agent workflows  

**Key Responsibilities:**
- Define and prioritize project goals
- Coordinate agent workflows
- Maintain alignment with EDE framework principles
- Review and approve significant architectural changes

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

- **Documentation Admin** receives one-way communication from **EDE Project Manager**
- **Documentation Admin** defers final authority to repository owner (@zfifteen)
- All agents must create pull requests for review; direct commits to main are prohibited
- Agents should reference relevant sections of other agents' instructions when coordination is needed

---

## Usage

When invoking an agent, reference the appropriate instruction file:
- For documentation tasks: Use `.github/agents/documentation_admin.md`
- For coding tasks: Use `.github/agents/incremental_coder_v2.md`
- For strategic planning: Consult `.github/agents/ede_project_manager.md`

---

**Repository:** `https://github.com/zfifteen/emergent-doom-engine`  
**Owner:** `zfifteen`  
**Last Updated:** January 4, 2026
