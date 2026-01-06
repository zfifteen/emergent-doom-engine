# Documentation Index for Experiment-095

## Overview

Experiment-095 demonstrates the Emergent Doom Engine (EDE) applied to bioinformatics: PAM detection using emergent sorting of wavelet-leader features. This index guides you to the right documentation for your needs.

## Documentation Files

### For Understanding the Experiment

**[README.md](README.md)** - Start here
- What the experiment does
- How it integrates with EDE
- Architecture overview
- Running instructions
- Success criteria

**[wave-crispr-signal.md](wave-crispr-signal.md)** - Experimental protocol
- Detailed methodology
- Dataset specifications
- Validation procedures
- Scientific references

### For Implementation

**[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick start for developers
- Minimal working Cell implementation
- Essential code patterns
- Common pitfalls
- Build and run commands

**[EDE_INTEGRATION_GUIDE.md](EDE_INTEGRATION_GUIDE.md)** - Comprehensive integration guide
- Step-by-step refactoring instructions
- Two implementation approaches (Option A vs B)
- Complete code examples
- Testing strategies
- Migration checklist

**[PRODUCTION_HARDENING.md](PRODUCTION_HARDENING.md)** - Production deployment
- Performance optimization
- Error handling
- Monitoring and observability
- Deployment patterns

## Reading Paths

### Path 1: "I want to understand what this experiment does"
1. Read [README.md](README.md) Overview and Purpose sections
2. Review Architecture: EDE Integration Points
3. Check Success Criteria table
4. Optionally: Skim [wave-crispr-signal.md](wave-crispr-signal.md) for scientific details

### Path 2: "I need to implement the EDE integration"
1. Review [README.md](README.md) Architecture section
2. Read [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for the essentials
3. Follow [EDE_INTEGRATION_GUIDE.md](EDE_INTEGRATION_GUIDE.md) step-by-step
4. Reference main [EDE README](../../README.md) for framework details
5. Test using patterns in QUICK_REFERENCE.md

### Path 3: "I'm deploying to production"
1. Complete EDE integration (Path 2)
2. Review [PRODUCTION_HARDENING.md](PRODUCTION_HARDENING.md)
3. Implement monitoring and error handling
4. Validate against [wave-crispr-signal.md](wave-crispr-signal.md) protocol requirements

### Path 4: "I want to understand EDE framework capabilities"
1. Read [README.md](README.md) EDE Framework Alignment section
2. Review architecture diagram
3. See main [EDE README](../../README.md) for complete framework documentation
4. This experiment serves as a **reference implementation** for bioinformatics use cases

## Key Concepts

### EDE Integration
- **Cell**: `WaveletFeatureCell` implements `Cell<WaveletFeatureCell>`
- **Execution**: Uses `CellBasedExecutionEngine` or `GenericCellExecutionEngine`
- **Metadata**: External metadata provider pattern for experiment tracking
- **Emergent Properties**: Robustness, clustering, delayed gratification, domain-agnostic

### Experiment Components
- **Domain-specific**: Feature extraction, dataset loading, MLP classifier, validation
- **EDE-powered**: Emergent sorting for tier assignment
- **Integration**: Wraps EDE execution in experiment-specific workflow

## Cross-References

### Internal Links
- [Main Experiment README](README.md)
- [Quick Reference](QUICK_REFERENCE.md)
- [Integration Guide](EDE_INTEGRATION_GUIDE.md)
- [Experimental Protocol](wave-crispr-signal.md)
- [Production Guide](PRODUCTION_HARDENING.md)

### EDE Framework Links
- [Main EDE README](../../README.md)
- [Cell Interface](../../src/main/java/com/emergent/doom/cell/Cell.java)
- [CellBasedExecutionEngine](../../src/main/java/com/emergent/doom/execution/CellBasedExecutionEngine.java)
- [AbstractCell](../../src/main/java/com/emergent/doom/cell/AbstractCell.java)

## Questions?

- **"How do I implement a Cell?"** → See [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
- **"What are the integration options?"** → See [EDE_INTEGRATION_GUIDE.md](EDE_INTEGRATION_GUIDE.md) Step 2
- **"What does this experiment validate?"** → See [README.md](README.md) Success Criteria
- **"How does this relate to Levin research?"** → See [README.md](README.md) EDE Framework Alignment
- **"What's the experimental protocol?"** → See [wave-crispr-signal.md](wave-crispr-signal.md)

## Updates

**Last Updated**: January 6, 2026  
**Status**: Documentation complete, awaiting code refactoring  
**Version**: Pre-integration (scaffolded components)

---

**Repository**: `https://github.com/zfifteen/emergent-doom-engine`  
**Experiment**: `lab/experiment-095/`  
**Framework**: [Emergent Doom Engine](../../README.md)
