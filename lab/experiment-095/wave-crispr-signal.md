# Executive Summary

This protocol tests the claim that an unsupervised emergent sorter applied to 28-dimensional stationary wavelet-leader signatures achieves 92% accuracy in PAM detection from nanopore FAST5 traces, outperforming baseline ridgelet+SVM by 12%. The experiment incorporates orthogonal validation, tests biological fidelity, assesses scalability limits, and integrates with the φ-geometry framework of wave-crispr-signal.[1][2][3]

## 1. Dataset Acquisition and Ground Truth Establishment

### 1.1 Primary Datasets

**Dataset A: CHANGE-seq Ground Truth Integration**[4][5][1]
- Acquire CHANGE-seq dataset (110 sgRNAs, 201,934 validated off-target sites in human primary T-cells) as biological ground truth for PAM functionality.[1][4]
- Extract genomic coordinates of confirmed on-target and off-target PAM sites (NGG motifs) with quantified cleavage activity.[4][1]
- Stratify sites into five classes by activity: on-target, high-activity off-target (Class A/B), medium-activity (Class C), low-activity (Class D), and non-functional NGG sites.[1][4]

**Dataset B: Nanopore FAST5 with Known PAM Context**[6][7][8]
- Download Oxford Nanopore benchmark datasets (GM24385, GIAB HG002-4) from AWS Open Data Registry.[8][6]
- Basecall using Guppy/Dorado to establish sequence-to-signal alignment.[7][6]
- Identify all NGG PAM sites in aligned reads using minimap2, retaining raw FAST5 signal windows (±50bp from PAM center).[7][8]
- Cross-reference with CHANGE-seq coordinates to label PAM windows as: **functional** (validated cleavage), **ambiguous** (NGG motif, no CHANGE-seq data), or **non-functional** (NGG in CHANGE-seq screen, zero activity).[4][1]

**Dataset C: Controlled Synthetic Dataset**[9][10][11]
- Generate 5,000 synthetic FAST5-like traces using a validated nanopore simulator with known ground truth.[12][11][13]
- Embed PAM-like step changes (representing polymerase stutter) at known positions with controlled signal-to-noise ratios (SNR = 1, 2, 4, 8).[11][13][12]
- Include confounding artifacts: baseline drift, transient spikes, and repetitive-sequence stutters.[12][11]

### 1.2 Sample Size and Power Analysis

- **Target**: 500 functional PAMs, 1,000 ambiguous NGG sites, 500 non-functional PAMs per flowcell chemistry (R9.4.1, R10.4.1) = **4,000 labeled windows**.[14][8][7]
- Power analysis (α=0.05, β=0.2): detect 10% accuracy difference between methods requires n≥380 per class.[15][9]

## 2. Feature Extraction: Stationary Wavelet Leaders

### 2.1 Implementation[16][17][12]

For each PAM-centered window (W_i):
1. Extract raw current trace $$I(t)$$, $$t \in [t_{\text{PAM}} - 250, t_{\text{PAM}} + 250]$$ (assuming 4 kHz sampling = ±62.5ms).[12][7]
2. Compute **stationary wavelet transform** (SWT) using Daubechies-4 (db4) wavelet to scales $$j = 1, 2, \ldots, 8$$.[18][16][12]
3. For each scale $$j$$, compute **wavelet leaders** $$L_j(k)$$ = local supremum of wavelet coefficients over dyadic intervals, capturing singularity strength.[17][19][16]
4. Extract 28D feature vector:
   - Wavelet leader statistics per scale (mean, std, skewness, kurtosis): $$8 \times 3 = 24$$ features.[16][12]
   - Multiscale entropy $$H_{\text{leader}}$$: 1 feature.[16][12]
   - Dominant scale $$j^*$$: 1 feature.[12][16]
   - Leader Hölder exponent $$\alpha_{\text{local}}$$: 2 features (min, max).[19][16]

### 2.2 Validation of Wavelet Basis Choice[20][21][15]

- **Hypothesis**: db4 optimally captures polymerase dwell-time singularities.[18][12]
- **Test**: Repeat feature extraction with Symlet-4, Coiflet-3, and data-driven wavelets (via lifting scheme on training subset).[21][15][20]
- **Metric**: Coherence with known PAM positions using wavelet transform coherence (WTC) method, threshold at 95% confidence.[15]
- **Acceptance criterion**: db4 WTC > 0.6 for PAM-centered windows; alternatives must exceed db4 by >5% to justify replacement.[15]

## 3. Emergent Sorter: Unsupervised Scale Pruning

### 3.1 Algorithm Implementation

**Input**: $$N = 4{,}000$$ feature vectors $$\mathbf{f}_i \in \mathbb{R}^{28}$$, $$i = 1, \ldots, N$$.

1. Initialize array in random order.[22][18]
2. Compute mean PAM pattern $$\boldsymbol{\mu}_{\text{PAM}}$$ from all vectors (no labels used).[22][18]
3. For $$T = 2{,}000$$ iterations:
   - Randomly select two indices $$i, j$$.[18][22]
   - Swap if $$d(\mathbf{f}_j, \boldsymbol{\mu}_{\text{PAM}}) < d(\mathbf{f}_i, \boldsymbol{\mu}_{\text{PAM}})$$ and $$i < j$$.[22][18]
   - Distance metric: Euclidean (baseline), Mahalanobis (robustness test).[12]
4. Output tiering: top 5% → **Tier 1**, next 25% → **Tier 2**, bottom 70% → **Tier 3**.[18][22]

### 3.2 Critical Validation Tests

**Test 3.2a: Convergence and Stability**[10][9]
- Run sorter with 10 different random seeds; measure tier assignment consistency via Cohen's κ.[9][10]
- **Acceptance**: κ > 0.8 (substantial agreement).[10][9]

**Test 3.2b: Robustness to Biased Libraries**[9][10]
- Construct three biased subsets: (i) purine-rich PAMs (>70% A/G in ±10bp), (ii) high-GC-content (>60%), (iii) repetitive-region PAMs (tandem repeats).[10][9]
- **Hypothesis**: If sorter amplifies bias, Tier 1 will over-represent biased subset PAMs beyond their true functional frequency.[9][10]
- **Metric**: Compare Tier 1 enrichment in biased subsets vs. unbiased control using permutation test (10,000 permutations).[15][9]
- **Acceptance**: p > 0.05 (no significant bias amplification).[9][15]

**Test 3.2c: Scale Pruning Interpretability**[16][12]
- After sorting, compute which of 28 dimensions contribute most to $$d(\mathbf{f}_i, \boldsymbol{\mu}_{\text{PAM}})$$ via Shapley values.[12]
- Cross-validate with known nanopore physics: scales $$j = 3\text{–}5$$ (∼10–40bp periodicity) should dominate, aligning with helical pitch and PAM-adjacent breathing dynamics.[16][18][12]
- **Acceptance**: Top 3 Shapley-ranked features include at least one scale in $$j = 3\text{–}5$$.[16][12]

## 4. Supervised Classification: Tiny MLP

### 4.1 Architecture and Training[13][11][12]

- **Input**: Tier labels (1, 2, 3) as categorical variable + 28D feature vector.[13][12]
- **Architecture**: 2 hidden layers (16, 8 neurons), ReLU activation, dropout (p=0.3), output sigmoid for binary PAM/non-PAM.[13][12]
- **Training**: 70% train, 15% validation, 15% test split; stratified by tier and functional class.[10][9]
- **Loss**: Binary cross-entropy with class weights (inverse frequency) to handle imbalance.[10][9]
- **Optimizer**: Adam, learning rate 0.001, early stopping (patience=10 epochs).[13][12]

### 4.2 Baseline Comparisons[23][11][12]

Compare emergent-editing pipeline against:
1. **Ridgelet+SVM** (claimed baseline): Extract ridgelet coefficients via Radon transform, train RBF-SVM (C=1, γ=auto).[11][12]
2. **Direct MLP**: Same MLP architecture, no emergent sorter (28D features only).[13][12]
3. **SquiggleNet-style CNN**: 1D CNN (3 conv layers, kernel size 5, max-pooling) adapted to PAM windows.[24][25]
4. **Threshold-based**: Amplitude threshold on raw signal (traditional nanopore event detection).[11][12]

## 5. Performance Metrics and Statistical Validation

### 5.1 Primary Metrics[23][11][9][10]

- **Accuracy**, **Precision**, **Recall**, **F1-score** on test set.[11][9][10]
- **Area under ROC curve (AUROC)** and **Precision-Recall AUC (AUPRC)**, stratified by functional class (on-target, high/medium/low off-target, non-functional).[23][9][10]
- **Spearman correlation** between predicted PAM scores and CHANGE-seq cleavage activity (quantitative validation).[23][1]

### 5.2 Statistical Significance Testing[17][15][16]

- **Bootstrap confidence intervals** (10,000 resamples) for accuracy difference between emergent editing and each baseline.[17][15][16]
- **Permutation test** (5,000 permutations): null hypothesis = emergent editing ≤ ridgelet+SVM.[17][15][16]
- **DeLong test** for AUROC comparisons.[9][10]
- **Significance threshold**: Bonferroni-corrected α = 0.05/4 = 0.0125 (four comparisons).[15][9]

### 5.3 Generalization Tests[26][27][23]

**Cross-Chemistry**: Train on R9.4.1, test on R10.4.1 (and vice versa).[28][14]
**Cross-Species**: Train on human (GM24385), test on synthetic *E. coli* dataset with SpCas9 PAMs.[29][28]
**Cross-SNR**: Test on synthetic data with SNR degraded to 1 (claimed impossible for threshold methods).[11][13][12]
**Expected result**: Accuracy drop <15% justifies robustness; larger drops mandate chemistry-specific recalibration.[27][26]

## 6. Biological Validation: Orthogonal Assays

### 6.1 Targeted Amplicon Sequencing[2][3][30]

- Select 50 PAM sites from each tier (150 total).[3][2]
- Transfect HEK293T cells with SpCas9 + sgRNAs targeting these sites.[31][2][3]
- Extract genomic DNA, PCR-amplify ±500bp around PAM, deep-sequence (Illumina, >10,000× coverage).[2][3]
- Quantify indel frequency using CRISPResso2.[3][2]
- **Hypothesis**: Tier 1 PAMs show significantly higher indel rates than Tier 3.[2][3]
- **Test**: Kruskal-Wallis test across tiers; post-hoc Dunn test with Bonferroni correction.[15][9]

### 6.2 TXTL-Based PAM Activity Assay[32][33]

- Synthesize linear dsDNA templates containing Tier 1 vs. Tier 3 PAM contexts.[32]
- Incubate in cell-free transcription-translation (TXTL) system with Cas9 + sgRNA.[32]
- Measure cleavage via qPCR or fluorescence reporter.[32]
- **Acceptance**: Tier 1 cleavage efficiency > Tier 3 by ≥2-fold.[33][32]

## 7. Integration with Wave-CRISPR-Signal Framework

### 7.1 φ-Geometry Fusion

- For each PAM window, compute φ-phase score $$\theta'(n, k^*)$$ with $$k^* \approx 0.3$$ using `wave_crispr_signal/features/phase_weighting.py`.[34][6]
- Compute Z-invariant score: $$Z = \text{sigmoid}\left(\frac{\Delta_{\text{spectral}}/\varphi}{\kappa(n, d)}\right)$$, where $$\kappa = d \cdot \ln(n+1)/e^2$$.[6][34]
- **Hybrid feature vector**: Concatenate [28D wavelet leaders, φ-phase, φ-curvature, Z-score] → 31D.[34][6]
- Retrain emergent sorter + MLP on hybrid features.[6][34]

### 7.2 Expected Synergy[34][6][18]

- **Hypothesis**: φ-geometry encodes sequence-level helical geometry; wavelet leaders encode signal-level dynamics. Combined, they should capture orthogonal aspects of PAM recognition (Cas9 binding vs. polymerase transduction).[6][34][18]
- **Test**: Compare AUPRC of hybrid model vs. wavelet-only and φ-only models using 5-fold cross-validation.[10][9]
- **Acceptance**: Hybrid AUPRC > max(wavelet-only, φ-only) by >3%.[9][10]

## 8. Scalability and Latency Benchmarking

### 8.1 Computational Profiling[35][36][37]

- **Hardware**: Test on (i) consumer laptop (8-core i7, 16GB RAM), (ii) MinION-companion GPU (NVIDIA Jetson Xavier), (iii) cloud instance (AWS c5.2xlarge).[37][35]
- **Workload**: Process 10,000 PAM windows sequentially; measure per-site latency (ms), memory footprint (MB), and power consumption (W, for Jetson only).[35][37]
- **Parallel scaling**: Test with 512–4,096 concurrent pores (simulated via multithreading).[37][35]
- **Acceptance**: Median latency <5 ms/site on laptop; <2 ms/site on Jetson; handles 4,096 pores without >10% latency degradation.[35][37]

### 8.2 Read-Until Integration Test[36][38][35]

- Implement mock read-until controller using MinKNOW API: accept/reject reads based on real-time tier assignment.[38][35]
- **Scenario**: Enrich for Tier 1 PAMs in a 1 Gb synthetic genome spike-in experiment.[38][35]
- **Metric**: Fold-enrichment of Tier 1 reads vs. uncontrolled sequencing; latency overhead vs. standard adaptive sampling.[38][35]
- **Acceptance**: ≥5× enrichment; latency <20% vs. standard.[35][38]

## 9. Failure Mode Analysis

### 9.1 Known Challenging Contexts[39][40][26]

Test on curated "hard cases":
- **Homopolymer runs** (≥6 identical bases) flanking PAM.[41][39]
- **Methylated cytosines** in PAM-adjacent region (5mC, 6mA).[26][14]
- **Structural variants** (inversions, duplications) disrupting expected signal.[42][39]
- **Low-complexity repeats** (STRs, LINEs).[39]

**Metric**: Compare emergent-editing accuracy in hard cases vs. standard cases.[26][39]
**Acceptance**: Accuracy drop <20% in any single category; document failure modes for future work.[39][26]

### 9.2 Adversarial Robustness[43][44][45]

- Inject synthetic noise into test FAST5 traces: Gaussian (σ = 0.5× baseline), impulse (salt-and-pepper, p=0.05), baseline drift (linear, quadratic).[11][12]
- **Acceptance**: Accuracy remains >75% under moderate noise; graceful degradation curve (not cliff).[12][11]

## 10. Ethical and Biosecurity Assessment

### 10.1 Dual-Use Evaluation[44][46][43]

- **Risk scenario**: Could rapid, unsupervised PAM tiering accelerate identification of high-specificity guides for pathogenic sequences?[46][43]
- **Mitigation**: Implement access controls for trained models; watermark outputs; establish data-sharing agreements requiring institutional biosafety review.[43][46]
- **Action**: Publish biosecurity self-assessment checklist alongside method (aligned with iGEM/NASEM guidelines).[46][43]

### 10.2 Equity and Access[44][43][46]

- **Question**: Does reliance on specialized hardware (MinION, GPU) exacerbate disparities?[44][46]
- **Test**: Benchmark on Raspberry Pi 4 (8GB) with INT8-quantized models.[44]
- **Acceptance**: Achieves >80% of full-precision accuracy; document minimum viable deployment.[44]

## 11. Reproducibility and Open Science

### 11.1 Code and Data Release[47][8][7]

- Deposit preprocessed FAST5 subsets (anonymized) to Zenodo/FigShare (DOI-minted).[8][7]
- Release Python package: `emergent_pam` with CLI tools, unit tests (pytest), and example workflows.[47][7]
- Provide Jupyter notebooks replicating all figures in manuscript.[7][47]
- Version control via GitHub; continuous integration (GitHub Actions) for regression testing.[47][7]

### 11.2 Independent Validation

- Pre-register experimental protocol on protocols.io before data collection.[30][31]
- Invite ≥2 independent labs to replicate core results (Sections 4–6) on their own nanopore datasets.[45][46]

## 12. Success Criteria and Decision Rules

| Criterion | Threshold | Action if Failed |
|-----------|-----------|------------------|
| Accuracy vs. ridgelet+SVM | +12% ± 3% (95% CI) | Revise feature extraction; investigate sorter convergence |
| AUPRC on CHANGE-seq correlation | Spearman ρ > 0.6 | Add biological priors (chromatin, expression) |
| Tier 1 indel validation | ≥60% show indels >5% | Lower Tier 1 cutoff; add orthogonal assay (e.g., GUIDE-seq) |
| Cross-chemistry generalization | Accuracy drop <15% | Train chemistry-specific sorters; add domain adaptation |
| Latency on laptop | <5 ms/site | Profile bottlenecks; consider FPGA acceleration |
| Biosecurity assessment | Pass iGEM checklist | Delay publication; engage ethics board |[46][43][35][2][9][1]

## 13. Timeline and Resource Allocation

| Phase | Duration | Key Deliverables |
|-------|----------|------------------|
| **Phase 1**: Dataset assembly & validation | 6 weeks | Curated FAST5 library, ground truth labels |
| **Phase 2**: Feature extraction & sorter implementation | 4 weeks | Validated wavelet-leader pipeline, convergence tests |
| **Phase 3**: Supervised learning & benchmarking | 6 weeks | Trained models, statistical comparisons |
| **Phase 4**: Biological validation | 8 weeks | TXTL assay, amplicon sequencing results |
| **Phase 5**: Integration & scalability | 4 weeks | Hybrid φ-geometry model, latency benchmarks |
| **Phase 6**: Failure analysis & ethics review | 4 weeks | Hard-case profiling, biosecurity assessment |
| **Phase 7**: Manuscript & code release | 4 weeks | Preprint, GitHub repo, protocols.io entry |
| **Total** | **36 weeks** | Peer-reviewed publication, open-source tool |[46][2][3][31]

## 14. Anticipated Challenges and Contingencies

**Challenge 1**: Insufficient FAST5 data with validated PAM ground truth.[8][7]
- *Contingency*: Augment with synthetic data validated via spike-in controls; collaborate with nanopore labs for proprietary datasets under NDA.[7][8]

**Challenge 2**: Emergent sorter fails to converge or shows seed-dependent instability.[22][18]
- *Contingency*: Replace with deterministic clustering (k-means, DBSCAN) as supervised feature selector; compare performance.[18][22]

**Challenge 3**: Biological validation shows no correlation between tiers and Cas9 activity.[3][1][2]
- *Contingency*: Investigate whether wavelet leaders capture signal artifacts rather than causal biology; pivot to explicit polymerase-kinetics modeling.[2][3]

**Challenge 4**: Latency exceeds real-time budget on consumer hardware.[37][35]
- *Contingency*: Implement GPU-accelerated wavelet transform (CuPy); explore fixed-point arithmetic (INT8) for edge deployment.[37][35]

## 15. Conclusion and Expected Impact

This protocol rigorously tests whether unsupervised emergent sorting of multiscale nanopore signal features can reliably identify functional CRISPR PAM sites, addressing key gaps in the original claim: biological ground truth validation, generalization across contexts, scalability verification, and ethical safeguards. If successful, this method would enable **real-time, label-free CRISPR guide validation directly from sequencing signals**, with applications in therapeutic design, genome engineering quality control, and adaptive sequencing. If unsuccessful, the systematic failure analysis will clarify the limits of signal-only approaches and motivate hybrid physics-biology-AI frameworks, exemplified by integration with wave-crispr-signal's φ-geometry priors.[43][46][1][6][2]

Sources
[1] CHANGE-seq reveals genetic and epigenetic effects on CRISPR ... https://pmc.ncbi.nlm.nih.gov/articles/PMC7652380/
[2] CRISPR Off-Target Validation - CD Genomics https://www.cd-genomics.com/crispr-off-target-validation.html
[3] GUIDE-Seq enables genome-wide profiling of off-target cleavage by ... https://pmc.ncbi.nlm.nih.gov/articles/PMC4320685/
[4] Learning to quantify uncertainty in off-target activity for CRISPR ... https://www.biorxiv.org/content/10.1101/2023.06.02.543468v2.full-text
[5] Learning to quantify uncertainty in off-target activity for CRISPR ... https://pmc.ncbi.nlm.nih.gov/articles/PMC11472043/
[6] Data analysis | Oxford Nanopore Technologies https://nanoporetech.com/document/data-analysis
[7] Fast nanopore sequencing data analysis with SLOW5 - PMC https://pmc.ncbi.nlm.nih.gov/articles/PMC9287168/
[8] Oxford Nanopore Technologies Benchmark Datasets https://registry.opendata.aws/ont-open-data/
[9] A benchmark of algorithms for the analysis of pooled CRISPR screens https://pmc.ncbi.nlm.nih.gov/articles/PMC7063732/
[10] Genome-aware annotation of CRISPR guides validates targets in ... https://pmc.ncbi.nlm.nih.gov/articles/PMC11590575/
[11] A Guide to Signal Processing Algorithms for Nanopore Sensors https://pubs.acs.org/doi/10.1021/acssensors.1c01618
[12] A Guide to Signal Processing Algorithms for Nanopore Sensors - PMC https://pmc.ncbi.nlm.nih.gov/articles/PMC8546757/
[13] [PDF] Deep Learning of Nanopore Sensing Signals Using a Bi-Path Network https://www.diva-portal.org/smash/get/diva2:1608826/FULLTEXT01.pdf
[14] Modified Base Best Practices and Benchmarking | EPI2ME Blog https://epi2me.nanoporetech.com/mod-validation-data/
[15] Statistical validation of wavelet transform coherence method to ... https://pmc.ncbi.nlm.nih.gov/articles/PMC3879179/
[16] Wavelet leaders and bootstrap for multifractal analysis of images https://www.sciencedirect.com/science/article/abs/pii/S0165168408004106
[17] [PDF] Bootstrap for log Wavelet Leaders Cumulant based Multifractal ... https://core.ac.uk/download/pdf/52329280.pdf
[18] [PDF] Analysis of Wavelet Transform Multiscale Products for Step ... - DTIC https://apps.dtic.mil/sti/pdfs/ADA351960.pdf
[19] [PDF] Contributions of Wavelet Leaders and Bootstrap to Multifractal ... https://www.semanticscholar.org/paper/Contributions-of-Wavelet-Leaders-and-Bootstrap-to-Wendt/35c37137b8b51bd302967e0af861ce99347932a2
[20] Wavelet Transform for Classification of EEG Signal using SVM and ... https://biomedpharmajournal.org/vol10no4/wavelet-transform-for-classification-of-eeg-signal-using-svm-and-ann/
[21] Wavelet basis functions in biomedical signal processing https://www.sciencedirect.com/science/article/abs/pii/S0957417410012881
[22] [PDF] Discovering multiscale and self-similar structure with data-driven ... https://dfloryan.github.io/publications/floryan2021discovering.pdf
[23] Learning to quantify uncertainty in off-target activity for CRISPR ... https://academic.oup.com/nar/article/52/18/e87/7757940
[24] SquiggleNet: real-time, direct classification of nanopore signals - NIH https://pmc.ncbi.nlm.nih.gov/articles/PMC8548853/
[25] [PDF] Real-Time, Direct Classification of Nanopore Signals with SquiggleNet https://www.biorxiv.org/content/10.1101/2021.01.15.426907v2.full.pdf
[26] A signal processing and deep learning framework for methylation ... https://www.nature.com/articles/s41467-024-45778-y
[27] Off-target effects in CRISPR-Cas genome editing for human ... https://www.sciencedirect.com/science/article/pii/S2162253125001908
[28] Assessing the efficacy of target adaptive sampling long-read ... https://www.nature.com/articles/s41525-024-00394-z
[29] Nanopore adaptive sampling effectively enriches bacterial plasmids https://journals.asm.org/doi/abs/10.1128/msystems.00945-23
[30] GUIDE-seq simplified library preparation protocol (CRISPR/Cas9 off ... https://www.protocols.io/view/guide-seq-simplified-library-preparation-protocol-wikfccw
[31] Comprehensive protocols for CRISPR/Cas9-based gene editing in ... https://pmc.ncbi.nlm.nih.gov/articles/PMC4988528/
[32] A TXTL-Based Assay to Rapidly Identify PAMs for CRISPR-Cas ... https://pubmed.ncbi.nlm.nih.gov/34985758/
[33] Selecting the Right Gene Editing Off-Target Assay - seqWell https://seqwell.com/guide-to-selecting-right-gene-editing-off-target-assay/
[34] [PDF] A Multi-Task Benchmark Dataset for Nanopore Sequencing ... https://proceedings.neurips.cc/paper_files/paper/2024/file/8bce223b376f52fb86a148097eebb10d-Supplemental-Datasets_and_Benchmarks_Track.pdf
[35] Real-time selective sequencing using nanopore technology - PMC https://pmc.ncbi.nlm.nih.gov/articles/PMC5008457/
[36] Targeted nanopore sequencing by real-time mapping of raw ... https://www.biorxiv.org/content/10.1101/2020.02.03.931923v1.full
[37] Accelerated Dynamic Time Warping on GPU for Selective Nanopore ... https://www.fortunejournals.com/articles/accelerated-dynamic-time-warping-on-gpu-for-selective-nanopore-sequencing.html
[38] adaptive sampling for selective nanopore sequencing https://nanoporetech.com/news/news-towards-real-time-targeting-enrichment-or-other-sampling-nanopore-sequencing-devices
[39] Genome-wide Mapping of Off-Target Events in Single-Stranded ... https://pmc.ncbi.nlm.nih.gov/articles/PMC5763015/
[40] Amplification-free long-read sequencing reveals unforeseen ... https://pubmed.ncbi.nlm.nih.gov/33261648/
[41] [PDF] Interpretable Feature Engineering for Nanopore Sequencing ... https://openreview.net/pdf?id=xEjie6Puap
[42] CRISPR-Cas9 induces large structural variants at on-target and off ... https://www.nature.com/articles/s41467-022-28244-5
[43] Disruptive technology: Exploring the ethical, legal, political ... - NIH https://pmc.ncbi.nlm.nih.gov/articles/PMC10157308/
[44] Portable nanopore-sequencing technology: Trends in development ... https://www.frontiersin.org/journals/microbiology/articles/10.3389/fmicb.2023.1043967/full
[45] Nanopore sequencing technology, bioinformatics and applications https://www.nature.com/articles/s41587-021-01108-x
[46] Real‐time genomics for One Health - PMC - PubMed Central https://pmc.ncbi.nlm.nih.gov/articles/PMC10407731/
[47] [PDF] A Multi-Task Benchmark Dataset for Nanopore Sequencing - NeurIPS https://proceedings.neurips.cc/paper_files/paper/2024/file/8bce223b376f52fb86a148097eebb10d-Paper-Datasets_and_Benchmarks_Track.pdf
[48] [PDF] Applications of Wavelet Transforms in Biomedical Optoacoustics https://digitalcommons.odu.edu/cgi/viewcontent.cgi?article=1506&context=biology_fac_pubs
[49] Deep Learning Based Models for CRISPR/Cas Off‐Target Prediction https://onlinelibrary.wiley.com/doi/10.1002/smtd.202500122
[50] Application of wavelet-based tools to study the dynamics of ... https://academic.oup.com/bib/article/7/4/375/184372
[51] Deep learning models simultaneously trained on multiple datasets ... https://www.nature.com/articles/s41467-025-65200-5
[52] [PDF] Machine Learning-Driven Nanopore Sensing for Quantitative, Label ... https://www.biorxiv.org/content/10.1101/2025.11.17.688909v2.full.pdf
[53] Analysis of Biological Signals Using Wavelet Coefficients for Finding ... https://www.rroij.com/open-access/analysis-of-biological-signals-using-wavelet-coefficients-for-finding-the-cardiac-diseases-and-their-severity.php?aid=34601
[54] CRISPR-FMC: a dual-branch hybrid network for ... - Frontiers https://www.frontiersin.org/journals/genome-editing/articles/10.3389/fgeed.2025.1643888/full
[55] A benchmark of computational methods for correcting biases of ... https://pmc.ncbi.nlm.nih.gov/articles/PMC11264729/
[56] Crispr system based droplet diagnostic systems and methods https://patents.google.com/patent/WO2020102610A1/en
[57] CRISPRroots: on- and off-target assessment of RNA-seq data in ... https://academic.oup.com/nar/article/50/4/e20/6445961
[58] Context-Seq: CRISPR-Cas9 targeted nanopore sequencing ... - Nature https://www.nature.com/articles/s41467-025-60491-0
[59] Oxford Nanopore long-read sequencing with CRISPR/Cas9 ... https://www.sciencedirect.com/science/article/pii/S1769721225000102
[60] Development and Validation of an Effective CRISPR/Cas9 Vector for ... https://www.frontiersin.org/journals/plant-science/articles/10.3389/fpls.2018.01533/full
[61] [PDF] Comparative analysis of CRISPR/Cas9-targeted nanopore ... - bioRxiv https://www.biorxiv.org/content/10.1101/2024.12.04.626786v2.full.pdf
[62] CRISPR screening by AAV episome-sequencing (CrAAVe-seq) https://www.nature.com/articles/s41593-025-02043-9
[63] Predicting CRISPR-Cas9 off-target effects in human primary cells ... https://academic.oup.com/bioinformaticsadvances/article/5/1/vbae184/7934878
[64] CRISPR-Cas9-guided amplification-free genomic diagnosis for ... https://journals.plos.org/plosone/article?id=10.1371%2Fjournal.pone.0297231
[65] Functional divergence of plant SCAR/WAVE proteins is determined ... https://pmc.ncbi.nlm.nih.gov/articles/PMC12094195/
[66] A Multi-Task Benchmark Dataset for Nanopore Sequencing - NeurIPS https://neurips.cc/virtual/2024/poster/97859
[67] Protocol for CRISPR screening by AAV episome sequencing ... https://www.protocols.io/view/protocol-for-crispr-screening-by-aav-episome-seque-81wgbz4o3gpk/v1
[68] Modeling CRISPR-Cas13d on-target and off-target effects using ... https://www.nature.com/articles/s41467-023-36316-3
[69] Nanopore Cas9‐targeted sequencing enables accurate and ... https://analyticalsciencejournals.onlinelibrary.wiley.com/doi/full/10.1002/bit.28382
