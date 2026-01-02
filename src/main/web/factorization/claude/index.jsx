import React, { useState, useCallback } from 'react';

// Mock data generator for RemainderCell states
const generateMockCells = (target, arraySize) => {
  const cells = [];
  for (let i = 1; i <= arraySize; i++) {
    const remainder = target % i;
    cells.push({
      position: i,
      remainder: remainder,
      isFactorFound: i > 1 && remainder === 0
    });
  }
  return cells;
};

// Generate mock trial result
const generateTrialResult = (target, arraySize, maxSteps) => {
  const steps = Math.floor(Math.random() * maxSteps * 0.3) + 100;
  const converged = Math.random() > 0.1;
  const cells = generateMockCells(target, arraySize);
  const factorFound = cells.some(c => c.isFactorFound);
  
  // Generate trajectory data (simplified)
  const trajectory = [];
  for (let step = 0; step <= Math.min(steps, 50); step++) {
    trajectory.push({
      step,
      avgRemainder: Math.max(0, target * 0.5 * Math.exp(-step / 20) + Math.random() * 100)
    });
  }
  
  return { steps, converged, cells, factorFound, trajectory };
};

// Color mapping based on remainder value
const getCellColor = (remainder, target, isFactorFound) => {
  if (isFactorFound) return '#e74c3c'; // Red - factor found
  if (remainder === 0) return '#e74c3c';
  
  const ratio = Number(remainder) / Number(target);
  if (ratio < 0.1) return '#f39c12'; // Orange - close
  if (ratio < 0.5) return '#3498db'; // Blue - medium
  return '#9b59b6'; // Purple - far
};

// Cell component
const CellTile = ({ cell, target, onHover }) => {
  const color = getCellColor(cell.remainder, target, cell.isFactorFound);
  
  return (
    <div
      className="w-3 h-3 rounded-sm cursor-pointer transition-transform hover:scale-150 hover:z-10"
      style={{ backgroundColor: color }}
      onMouseEnter={() => onHover(cell)}
      onMouseLeave={() => onHover(null)}
      title={`Pos: ${cell.position}, Rem: ${cell.remainder}`}
    />
  );
};

// Main Dashboard
export default function FactorizationDashboard() {
  // Configuration state
  const [config, setConfig] = useState({
    semiprime: 100099,
    arraySize: 1000,
    trials: 5,
    maxSteps: 50000,
    stableSteps: 3
  });
  
  // Results state
  const [results, setResults] = useState(null);
  const [isRunning, setIsRunning] = useState(false);
  const [hoveredCell, setHoveredCell] = useState(null);
  const [selectedTrial, setSelectedTrial] = useState(0);
  const [currentStep, setCurrentStep] = useState(0);
  
  // Run experiment
  const runExperiment = useCallback(() => {
    setIsRunning(true);
    setResults(null);
    
    // Simulate async execution
    setTimeout(() => {
      const trialResults = [];
      for (let i = 0; i < config.trials; i++) {
        trialResults.push(generateTrialResult(config.semiprime, config.arraySize, config.maxSteps));
      }
      
      const factorsFound = trialResults.filter(t => t.factorFound).length;
      const meanSteps = trialResults.reduce((sum, t) => sum + t.steps, 0) / trialResults.length;
      const convergenceRate = trialResults.filter(t => t.converged).length / trialResults.length * 100;
      
      // Find actual factors
      const factors = [];
      for (let i = 2; i <= Math.sqrt(config.semiprime); i++) {
        if (config.semiprime % i === 0) {
          factors.push(i, config.semiprime / i);
          break;
        }
      }
      
      setResults({
        trials: trialResults,
        summary: {
          factorFound: factorsFound > 0,
          factors: factors,
          meanSteps: Math.round(meanSteps),
          convergenceRate: convergenceRate.toFixed(1),
          totalTrials: config.trials
        }
      });
      setSelectedTrial(0);
      setCurrentStep(0);
      setIsRunning(false);
    }, 500);
  }, [config]);
  
  const updateConfig = (key, value) => {
    setConfig(prev => ({ ...prev, [key]: parseInt(value) || 0 }));
  };
  
  const currentTrial = results?.trials[selectedTrial];
  const displayCells = currentTrial?.cells.slice(0, Math.min(config.arraySize, 2000)) || [];
  
  return (
    <div className="min-h-screen bg-gray-900 text-gray-100 p-4">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-2xl font-bold text-purple-400 mb-4">Emergent Doom Engine — Factorization Experiment</h1>
        
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
          {/* Configuration Panel */}
          <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
            <h2 className="text-lg font-semibold text-blue-400 mb-3">Configuration</h2>
            
            <div className="space-y-3">
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Target Semiprime
                  <span className="ml-1 text-xs text-gray-500" title="The number to factor">[?]</span>
                </label>
                <input
                  type="number"
                  value={config.semiprime}
                  onChange={(e) => updateConfig('semiprime', e.target.value)}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
              
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Array Size
                  <span className="ml-1 text-xs text-gray-500" title="Number of RemainderCell agents">[?]</span>
                </label>
                <input
                  type="number"
                  value={config.arraySize}
                  onChange={(e) => updateConfig('arraySize', e.target.value)}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
              
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Trials
                  <span className="ml-1 text-xs text-gray-500" title="Number of experiment runs">[?]</span>
                </label>
                <input
                  type="number"
                  value={config.trials}
                  onChange={(e) => updateConfig('trials', e.target.value)}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
              
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Max Steps
                  <span className="ml-1 text-xs text-gray-500" title="Maximum iterations before timeout">[?]</span>
                </label>
                <input
                  type="number"
                  value={config.maxSteps}
                  onChange={(e) => updateConfig('maxSteps', e.target.value)}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
              
              <div>
                <label className="block text-sm text-gray-400 mb-1">
                  Stable Steps
                  <span className="ml-1 text-xs text-gray-500" title="Convergence threshold">[?]</span>
                </label>
                <input
                  type="number"
                  value={config.stableSteps}
                  onChange={(e) => updateConfig('stableSteps', e.target.value)}
                  className="w-full bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
              
              <button
                onClick={runExperiment}
                disabled={isRunning}
                className="w-full bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 text-white font-semibold py-2 px-4 rounded transition-colors"
              >
                {isRunning ? 'Running...' : 'Run Experiment'}
              </button>
            </div>
            
            {/* Legend */}
            <div className="mt-4 pt-4 border-t border-gray-700">
              <h3 className="text-sm font-medium text-gray-400 mb-2">Cell Legend</h3>
              <div className="space-y-1 text-xs">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded" style={{ backgroundColor: '#e74c3c' }}></div>
                  <span>Factor Found (rem=0)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded" style={{ backgroundColor: '#f39c12' }}></div>
                  <span>Close (&lt;10%)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded" style={{ backgroundColor: '#3498db' }}></div>
                  <span>Medium (&lt;50%)</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded" style={{ backgroundColor: '#9b59b6' }}></div>
                  <span>Far (&gt;50%)</span>
                </div>
              </div>
            </div>
          </div>
          
          {/* Main Visualization Area */}
          <div className="lg:col-span-3 space-y-4">
            {/* Cell State Grid */}
            <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
              <div className="flex justify-between items-center mb-3">
                <h2 className="text-lg font-semibold text-blue-400">Cell State Grid</h2>
                {hoveredCell && (
                  <div className="text-sm text-gray-300 bg-gray-700 px-3 py-1 rounded">
                    Position: <span className="text-purple-400 font-mono">{hoveredCell.position}</span> | 
                    Remainder: <span className="text-orange-400 font-mono">{hoveredCell.remainder}</span>
                    {hoveredCell.isFactorFound && <span className="ml-2 text-red-400 font-bold">★ FACTOR</span>}
                  </div>
                )}
              </div>
              
              {displayCells.length > 0 ? (
                <div className="flex flex-wrap gap-0.5 p-2 bg-gray-900 rounded max-h-64 overflow-auto">
                  {displayCells.map((cell, idx) => (
                    <CellTile 
                      key={idx} 
                      cell={cell} 
                      target={config.semiprime}
                      onHover={setHoveredCell}
                    />
                  ))}
                </div>
              ) : (
                <div className="h-32 flex items-center justify-center text-gray-500">
                  Run experiment to visualize cell states
                </div>
              )}
            </div>
            
            {/* Trajectory + Trial Results Row */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Trajectory Panel */}
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
                <h2 className="text-lg font-semibold text-blue-400 mb-3">Trajectory</h2>
                
                {currentTrial?.trajectory ? (
                  <div>
                    {/* ASCII-style trajectory visualization */}
                    <div className="font-mono text-xs bg-gray-900 p-2 rounded h-32 overflow-auto">
                      {currentTrial.trajectory.map((point, idx) => {
                        const barLength = Math.min(40, Math.floor(point.avgRemainder / (config.semiprime / 40)));
                        return (
                          <div key={idx} className="flex items-center">
                            <span className="text-gray-500 w-8">{String(point.step).padStart(3)}</span>
                            <span className="text-green-400">{'█'.repeat(barLength)}</span>
                            <span className="text-gray-600">{'░'.repeat(40 - barLength)}</span>
                          </div>
                        );
                      })}
                    </div>
                    
                    {/* Step slider */}
                    <div className="mt-3">
                      <label className="block text-sm text-gray-400 mb-1">
                        Step: {currentStep} / {currentTrial.steps}
                      </label>
                      <input
                        type="range"
                        min="0"
                        max={Math.min(currentTrial.trajectory.length - 1, 50)}
                        value={currentStep}
                        onChange={(e) => setCurrentStep(parseInt(e.target.value))}
                        className="w-full"
                      />
                    </div>
                  </div>
                ) : (
                  <div className="h-32 flex items-center justify-center text-gray-500">
                    No trajectory data
                  </div>
                )}
              </div>
              
              {/* Trial Results */}
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
                <h2 className="text-lg font-semibold text-blue-400 mb-3">Trial Results</h2>
                
                {results?.trials ? (
                  <div className="space-y-2 max-h-40 overflow-auto">
                    {results.trials.map((trial, idx) => (
                      <div
                        key={idx}
                        onClick={() => setSelectedTrial(idx)}
                        className={`flex items-center justify-between p-2 rounded cursor-pointer transition-colors ${
                          selectedTrial === idx 
                            ? 'bg-purple-900/50 border border-purple-500' 
                            : 'bg-gray-700 hover:bg-gray-600'
                        }`}
                      >
                        <span className="font-medium">Trial {idx + 1}</span>
                        <div className="flex items-center gap-3">
                          <span className="text-sm text-gray-400">{trial.steps} steps</span>
                          {trial.factorFound ? (
                            <span className="text-green-400 text-sm">✓ Found</span>
                          ) : (
                            <span className="text-red-400 text-sm">✗ Not found</span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="h-32 flex items-center justify-center text-gray-500">
                    No trials completed
                  </div>
                )}
              </div>
            </div>
            
            {/* Summary Metrics */}
            <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
              <h2 className="text-lg font-semibold text-blue-400 mb-3">Summary Metrics</h2>
              
              {results?.summary ? (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="bg-gray-900 rounded p-3 text-center">
                    <div className="text-sm text-gray-400 mb-1">Factor Found</div>
                    <div className={`text-2xl font-bold ${results.summary.factorFound ? 'text-green-400' : 'text-red-400'}`}>
                      {results.summary.factorFound ? 'YES' : 'NO'}
                    </div>
                    {results.summary.factors.length > 0 && (
                      <div className="text-xs text-purple-400 mt-1">
                        {results.summary.factors.join(' × ')}
                      </div>
                    )}
                  </div>
                  
                  <div className="bg-gray-900 rounded p-3 text-center">
                    <div className="text-sm text-gray-400 mb-1">Mean Steps</div>
                    <div className="text-2xl font-bold text-blue-400">
                      {results.summary.meanSteps.toLocaleString()}
                    </div>
                  </div>
                  
                  <div className="bg-gray-900 rounded p-3 text-center">
                    <div className="text-sm text-gray-400 mb-1">Convergence Rate</div>
                    <div className="text-2xl font-bold text-orange-400">
                      {results.summary.convergenceRate}%
                    </div>
                  </div>
                  
                  <div className="bg-gray-900 rounded p-3 text-center">
                    <div className="text-sm text-gray-400 mb-1">Total Trials</div>
                    <div className="text-2xl font-bold text-purple-400">
                      {results.summary.totalTrials}
                    </div>
                  </div>
                </div>
              ) : (
                <div className="h-20 flex items-center justify-center text-gray-500">
                  Run experiment to see summary
                </div>
              )}
            </div>
          </div>
        </div>
        
        {/* Footer */}
        <div className="mt-4 text-center text-xs text-gray-600">
          Emergent Doom Engine v1.0 — RemainderCell Agent Visualization
        </div>
      </div>
    </div>
  );
}
