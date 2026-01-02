import React, { useState, useEffect, useMemo } from 'react';
import { Play, RotateCcw, Settings, Activity, CheckCircle, AlertTriangle, Info, Database, Zap } from 'lucide-react';

/**
 * MOCK UI IMPLEMENTATION
 * Based on Factorization Experiment UI Requirements v1.0
 */

const Card = ({ children, className = "" }) => (
    <div className={`bg-slate-800 border border-slate-700 rounded-lg p-4 shadow-sm ${className}`}>
        {children}
    </div>
);

const Badge = ({ children, type = "neutral" }) => {
    const colors = {
        success: "bg-emerald-900/50 text-emerald-300 border-emerald-700",
        danger: "bg-red-900/50 text-red-300 border-red-700",
        neutral: "bg-slate-700 text-slate-300 border-slate-600",
        warning: "bg-amber-900/50 text-amber-300 border-amber-700"
    };
    return (
        <span className={`px-2 py-0.5 rounded text-xs font-medium border ${colors[type]}`}>
      {children}
    </span>
    );
};

export default function FactorizationDashboard() {
    // --- State: Configuration (FR-CFG-01 to 06) ---
    const [config, setConfig] = useState({
        targetSemiprime: 100099,
        arraySize: 1000,
        numTrials: 5,
        maxSteps: 50000,
        stableSteps: 3
    });

    // --- State: Experiment Execution ---
    const [isRunning, setIsRunning] = useState(false);
    const [progress, setProgress] = useState(0);
    const [currentTrial, setCurrentTrial] = useState(0);

    // --- State: Results ---
    const [cells, setCells] = useState([]);
    const [trialHistory, setTrialHistory] = useState([]);
    const [metrics, setMetrics] = useState({
        factorFound: false,
        meanSteps: 0,
        convergenceRate: 0,
        totalTrials: 0
    });

    // --- Helper: Color Logic (FR-VIZ-02) ---
    const getCellColor = (remainder, target) => {
        if (remainder === 0) return 'bg-red-500 hover:bg-red-400 ring-2 ring-red-500 z-10'; // Factor Found

        // Gradient based on remainder magnitude relative to target
        const ratio = remainder / target;

        if (ratio < 0.1) return 'bg-orange-500 hover:bg-orange-400'; // Close call
        if (ratio < 0.5) return 'bg-blue-600 hover:bg-blue-500';     // Medium
        return 'bg-purple-900 hover:bg-purple-700';                  // Far
    };

    // --- Simulation Logic ---
    const runSimulation = () => {
        setIsRunning(true);
        setProgress(0);
        setTrialHistory([]);
        setCurrentTrial(1);

        // Reset Metrics
        setMetrics({
            factorFound: false,
            meanSteps: 0,
            convergenceRate: 0,
            totalTrials: 0
        });

        // Simulate async execution steps
        let step = 0;
        const totalSteps = 20; // Visual simulation steps

        const interval = setInterval(() => {
            step++;
            const currentProg = (step / totalSteps) * 100;
            setProgress(currentProg);

            // Generate visual noise (mocking dynamic cell updates)
            const mockCells = Array.from({ length: config.arraySize }, (_, i) => {
                const position = i + 1;
                // The actual math logic from the backend requirements
                const remainder = config.targetSemiprime % position;
                return { position, remainder };
            });
            setCells(mockCells);

            if (step >= totalSteps) {
                clearInterval(interval);
                finishSimulation(mockCells);
            }
        }, 100);
    };

    const finishSimulation = (finalCells) => {
        setIsRunning(false);

        // Calculate simulated results
        const factors = finalCells.filter(c => c.remainder === 0 && c.position > 1 && c.position < config.targetSemiprime);
        const found = factors.length > 0;

        // Generate Mock Trial History
        const history = Array.from({ length: config.numTrials }, (_, i) => ({
            id: i + 1,
            steps: Math.floor(Math.random() * (2000 - 1000) + 1000), // Random steps between 1000-2000
            converged: true,
            foundFactor: found
        }));

        setTrialHistory(history);

        // Update Aggregate Metrics
        setMetrics({
            factorFound: found,
            meanSteps: Math.round(history.reduce((acc, curr) => acc + curr.steps, 0) / history.length),
            convergenceRate: 100, // Mocking 100% convergence for this demo
            totalTrials: config.numTrials
        });
    };

    const handleConfigChange = (e) => {
        const { name, value } = e.target;
        setConfig(prev => ({ ...prev, [name]: parseInt(value) || 0 }));
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-200 font-sans p-4 md:p-8">

            {/* Header */}
            <header className="mb-8 flex flex-col md:flex-row md:items-center justify-between border-b border-slate-800 pb-4">
                <div>
                    <h1 className="text-2xl font-bold text-white flex items-center gap-2">
                        <Activity className="text-blue-500" />
                        Factorization Experiment <span className="text-slate-500 font-normal text-lg">UI Mockup v1.0</span>
                    </h1>
                    <p className="text-slate-400 text-sm mt-1">Emergent Doom Engine • RemainderCell Dynamics</p>
                </div>
                <div className="mt-4 md:mt-0 flex gap-3">
                    <Badge type="neutral">Status: Approved</Badge>
                    <Badge type="neutral">Ver: 1.0</Badge>
                </div>
            </header>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

                {/* LEFT COLUMN: Configuration (FR-CFG) */}
                <aside className="lg:col-span-3 space-y-6">
                    <Card className="bg-slate-900 border-slate-700">
                        <div className="flex items-center gap-2 mb-4 text-slate-100 font-semibold border-b border-slate-700 pb-2">
                            <Settings size={18} />
                            Configuration
                        </div>

                        <div className="space-y-4">
                            {/* Input: Target Semiprime */}
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-1 flex justify-between">
                                    Target Semiprime
                                    <Info size={12} className="text-slate-500 cursor-help" title="The integer to factorize" />
                                </label>
                                <input
                                    type="number"
                                    name="targetSemiprime"
                                    value={config.targetSemiprime}
                                    onChange={handleConfigChange}
                                    className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-white focus:ring-2 focus:ring-blue-500 focus:outline-none transition-all"
                                />
                            </div>

                            {/* Input: Array Size */}
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-1 flex justify-between">
                                    Array Size (Agents)
                                    <Info size={12} className="text-slate-500 cursor-help" title="Number of RemainderCell agents" />
                                </label>
                                <input
                                    type="number"
                                    name="arraySize"
                                    value={config.arraySize}
                                    onChange={handleConfigChange}
                                    className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-white focus:ring-2 focus:ring-blue-500 focus:outline-none"
                                />
                            </div>

                            {/* Grid: Smaller Configs */}
                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-xs font-medium text-slate-400 mb-1">Trials</label>
                                    <input
                                        type="number"
                                        name="numTrials"
                                        value={config.numTrials}
                                        onChange={handleConfigChange}
                                        className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-sm text-white"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-slate-400 mb-1">Max Steps</label>
                                    <input
                                        type="number"
                                        name="maxSteps"
                                        value={config.maxSteps}
                                        onChange={handleConfigChange}
                                        className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-sm text-white"
                                    />
                                </div>
                            </div>

                            <div className="pt-4">
                                <button
                                    onClick={runSimulation}
                                    disabled={isRunning}
                                    className={`w-full flex items-center justify-center gap-2 py-3 rounded font-bold transition-all shadow-lg
                    ${isRunning
                                        ? 'bg-slate-700 text-slate-400 cursor-not-allowed'
                                        : 'bg-blue-600 hover:bg-blue-500 text-white hover:shadow-blue-500/25'
                                    }`}
                                >
                                    {isRunning ? (
                                        <>
                                            <RotateCcw className="animate-spin" size={18} />
                                            Running...
                                        </>
                                    ) : (
                                        <>
                                            <Play size={18} fill="currentColor" />
                                            Run Experiment
                                        </>
                                    )}
                                </button>
                            </div>
                        </div>
                    </Card>

                    {/* Trial History Panel (FR-TRJ-02) */}
                    <Card className="h-96 overflow-hidden flex flex-col">
                        <div className="flex items-center gap-2 mb-2 text-slate-100 font-semibold text-sm">
                            <Database size={16} />
                            Trial History
                        </div>
                        <div className="flex-1 overflow-y-auto pr-2 space-y-2">
                            {trialHistory.length === 0 ? (
                                <div className="text-center text-slate-600 text-xs italic mt-10">No trials run yet.</div>
                            ) : (
                                trialHistory.map((trial) => (
                                    <div key={trial.id} className="bg-slate-900/50 p-3 rounded border border-slate-700/50 flex justify-between items-center">
                                        <div>
                                            <div className="text-xs text-slate-400">Trial #{trial.id}</div>
                                            <div className="text-sm font-mono text-slate-200">{trial.steps} steps</div>
                                        </div>
                                        <div>
                                            {trial.foundFactor ? (
                                                <Badge type="success">Found</Badge>
                                            ) : (
                                                <Badge type="warning">No Factor</Badge>
                                            )}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </Card>
                </aside>

                {/* RIGHT COLUMN: Visualization & Metrics */}
                <main className="lg:col-span-9 flex flex-col gap-6">

                    {/* Top Row: Metrics (FR-MET) */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <Card className="flex flex-col justify-center items-center py-6 bg-slate-900/80">
                            <div className="text-slate-400 text-xs uppercase tracking-wider mb-1">Factor Found</div>
                            <div className="text-xl font-bold flex items-center gap-2">
                                {metrics.factorFound ? (
                                    <span className="text-emerald-400 flex items-center gap-1"><CheckCircle size={20} /> YES</span>
                                ) : (
                                    <span className="text-slate-500 flex items-center gap-1"><AlertTriangle size={20} /> NO</span>
                                )}
                            </div>
                        </Card>

                        <Card className="flex flex-col justify-center items-center py-6 bg-slate-900/80">
                            <div className="text-slate-400 text-xs uppercase tracking-wider mb-1">Mean Steps</div>
                            <div className="text-2xl font-mono text-white">
                                {metrics.meanSteps > 0 ? metrics.meanSteps.toLocaleString() : "—"}
                            </div>
                        </Card>

                        <Card className="flex flex-col justify-center items-center py-6 bg-slate-900/80">
                            <div className="text-slate-400 text-xs uppercase tracking-wider mb-1">Convergence</div>
                            <div className="text-2xl font-mono text-blue-400">
                                {metrics.convergenceRate}%
                            </div>
                        </Card>

                        <Card className="flex flex-col justify-center items-center py-6 bg-slate-900/80">
                            <div className="text-slate-400 text-xs uppercase tracking-wider mb-1">Total Trials</div>
                            <div className="text-2xl font-mono text-white">
                                {metrics.totalTrials}
                            </div>
                        </Card>
                    </div>

                    {/* Middle: Progress Bar (FR-CTL-02) */}
                    {isRunning && (
                        <div className="w-full bg-slate-800 rounded-full h-2.5 mb-2 overflow-hidden border border-slate-700">
                            <div
                                className="bg-blue-600 h-2.5 rounded-full transition-all duration-300 ease-out"
                                style={{ width: `${progress}%` }}
                            ></div>
                        </div>
                    )}

                    {/* Main Visualization Area (FR-VIZ) */}
                    <div className="flex-1 bg-slate-900 rounded-xl border border-slate-700 shadow-inner p-4 relative overflow-hidden min-h-[500px] flex flex-col">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-slate-200 font-semibold flex items-center gap-2">
                                <Zap size={18} className="text-orange-500" />
                                RemainderCell State Grid
                            </h2>
                            <div className="flex gap-4 text-xs">
                                <div className="flex items-center gap-1"><div className="w-3 h-3 bg-red-500 rounded-sm"></div> Factor (0)</div>
                                <div className="flex items-center gap-1"><div className="w-3 h-3 bg-orange-500 rounded-sm"></div> Low</div>
                                <div className="flex items-center gap-1"><div className="w-3 h-3 bg-blue-600 rounded-sm"></div> Med</div>
                                <div className="flex items-center gap-1"><div className="w-3 h-3 bg-purple-900 rounded-sm"></div> High</div>
                            </div>
                        </div>

                        {/* The Grid */}
                        {cells.length === 0 ? (
                            <div className="flex-1 flex flex-col items-center justify-center text-slate-600 border-2 border-dashed border-slate-800 rounded-lg">
                                <Activity size={48} className="mb-4 opacity-20" />
                                <p>Ready to Initialize Experiment</p>
                                <p className="text-sm mt-2">Set parameters and click Run</p>
                            </div>
                        ) : (
                            <div className="flex-1 overflow-y-auto custom-scrollbar">
                                <div className="grid grid-cols-[repeat(auto-fill,minmax(20px,1fr))] gap-1 p-1">
                                    {cells.map((cell) => (
                                        <div
                                            key={cell.position}
                                            className={`aspect-square rounded-sm transition-colors cursor-pointer group relative ${getCellColor(cell.remainder, config.targetSemiprime)}`}
                                        >
                                            {/* Tooltip (FR-VIZ-03) */}
                                            <div className="hidden group-hover:block absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-32 bg-slate-800 text-white text-xs rounded p-2 shadow-xl border border-slate-600 z-50 pointer-events-none">
                                                <div className="font-bold text-slate-300">Pos: {cell.position}</div>
                                                <div>Rem: <span className="font-mono text-blue-300">{cell.remainder}</span></div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Overlay for large arrays to explain truncation if needed */}
                        <div className="absolute bottom-2 right-2 text-[10px] text-slate-600">
                            Rendering {config.arraySize} agents
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}