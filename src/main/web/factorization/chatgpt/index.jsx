import React, { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Slider } from "@/components/ui/slider";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { AlertTriangle, CheckCircle2, Loader2, Play, Upload } from "lucide-react";

/**
 * Mock UI for FactorizationExperiment.java
 *
 * Goals supported:
 * - Visualize emergent factorization dynamics
 * - Enable real-time and post-hoc experiment analysis
 *
 * Primary UX: cell state grid (final states), then trajectories, then mean steps.
 * Ready to wire to backend via REST/WebSocket.
 */

// -----------------------------
// Types (front-end view models)
// -----------------------------

type RemainderCell = {
  target: string; // BigInteger as string
  position: number;
  remainder: string; // BigInteger as string
};

type TrialResult = {
  trialIndex: number;
  steps: number;
  converged: boolean;
  factorFound: boolean;
  factorPosition?: number;
  finalCells: RemainderCell[];
  // optional trajectory: array of steps, each step has small sample of cell remainders
  trajectory?: {
    step: number;
    // keep it light for mock; in real app this could be full grid or sampled positions
    samples: { position: number; remainder: string }[];
  }[];
};

type ExperimentSummary = {
  factorFound: boolean;
  meanSteps: number;
  convergenceRatePct: number;
  trialsRun: number;
};

// -----------------------------
// Constants
// -----------------------------

const DEFAULTS = {
  semiprime: "100099",
  arraySize: 1000,
  trials: 5,
  maxSteps: 50000,
  stableSteps: 3,
};

const REMAINDER_COLORS = {
  zero: "#e74c3c", // factor found
  low: "#f39c12",
  med: "#3498db",
  high: "#9b59b6",
} as const;

// -----------------------------
// Helpers
// -----------------------------

function clamp(n: number, lo: number, hi: number) {
  return Math.max(lo, Math.min(hi, n));
}

function safeInt(s: string, fallback: number) {
  const n = Number(String(s).replaceAll("_", "").trim());
  return Number.isFinite(n) ? Math.floor(n) : fallback;
}

function formatInt(n: number) {
  return n.toLocaleString();
}

function hash32(str: string) {
  // simple deterministic hash for mock visuals
  let h = 2166136261;
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return h >>> 0;
}

function pickColorForRemainder(remainder: string, target: string) {
  // Requirements: red=0; gradient buckets for others.
  // In mock: approximate by (remainder mod 100) bucket so it is stable without BigInt math.
  const r = remainder.trim();
  if (r === "0") return REMAINDER_COLORS.zero;
  const bucket = hash32(`${r}|${target}`) % 100;
  if (bucket < 10) return REMAINDER_COLORS.low; // 0-10%
  if (bucket < 60) return REMAINDER_COLORS.med;
  return REMAINDER_COLORS.high;
}

function isFactorCell(cell: RemainderCell) {
  return cell.position > 1 && cell.remainder === "0";
}

function buildMockTrial(
  trialIndex: number,
  cfg: {
    semiprime: string;
    arraySize: number;
    maxSteps: number;
    stableSteps: number;
  }
): TrialResult {
  const { semiprime, arraySize, maxSteps, stableSteps } = cfg;

  // mock convergence and steps (deterministic)
  const seed = hash32(`${semiprime}|${arraySize}|${trialIndex}`);
  const converged = (seed % 100) > 12; // ~88% converged
  const steps = clamp(
    500 + (seed % 12000) + Math.floor(arraySize / 3),
    stableSteps,
    Math.min(maxSteps, 50000)
  );

  // mock factor found: make it occasional and deterministic
  const factorFound = (seed % 100) < 35; // ~35% success in mock
  const factorPosition = factorFound ? 2 + (seed % Math.min(997, arraySize - 1)) : undefined;

  const finalCells: RemainderCell[] = Array.from({ length: arraySize }, (_, i) => {
    const position = i + 1;
    const remainder = factorFound && position === factorPosition ? "0" : String(hash32(`${seed}:${position}`) % 100000);
    return { target: semiprime, position, remainder };
  });

  // lightweight trajectory samples (positions spread across array)
  const samplePositions = useStableSamples(arraySize, 24, seed);
  const trajectory = Array.from({ length: 40 }, (_, j) => {
    const step = Math.floor((steps / 39) * j);
    return {
      step,
      samples: samplePositions.map((pos) => {
        const rem = factorFound && pos === factorPosition && j > 25 ? "0" : String(hash32(`${seed}|${pos}|${j}`) % 100000);
        return { position: pos, remainder: rem };
      }),
    };
  });

  return {
    trialIndex,
    steps,
    converged,
    factorFound,
    factorPosition,
    finalCells,
    trajectory,
  };
}

function useStableSamples(arraySize: number, count: number, seed: number) {
  // deterministic pseudo-random distinct positions
  const picks = new Set<number>();
  let x = seed || 1;
  while (picks.size < Math.min(count, arraySize)) {
    x = (Math.imul(x, 1664525) + 1013904223) >>> 0;
    const pos = 1 + (x % arraySize);
    picks.add(pos);
  }
  return Array.from(picks).sort((a, b) => a - b);
}

function summarize(trials: TrialResult[]): ExperimentSummary {
  const trialsRun = trials.length;
  const factorFound = trials.some((t) => t.factorFound);
  const meanSteps = trialsRun
    ? Math.round(trials.reduce((acc, t) => acc + t.steps, 0) / trialsRun)
    : 0;
  const convergenceRatePct = trialsRun
    ? Math.round((100 * trials.filter((t) => t.converged).length) / trialsRun)
    : 0;
  return { factorFound, meanSteps, convergenceRatePct, trialsRun };
}

// -----------------------------
// Backend wiring (stubs)
// -----------------------------

/**
 * REST example:
 * POST /api/factorization/experiment/run
 * body: { targetSemiprime, arraySize, trials, maxSteps, stableSteps, recordTrajectory }
 * response: { trials: TrialResult[], summary: ExperimentSummary }
 */
async function runExperimentViaRest(_payload: any) {
  // Replace with real fetch in integration.
  // const res = await fetch('/api/factorization/experiment/run', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
  // return await res.json();
  return null;
}

/**
 * WebSocket example:
 * ws://.../ws/experiment
 * messages: {type:'progress', pct, trialIndex, step} | {type:'final', trials, summary}
 */
function connectExperimentWebSocket(_onMessage: (msg: any) => void) {
  // Replace with real WebSocket in integration.
  // const ws = new WebSocket(url); ws.onmessage = (e)=>onMessage(JSON.parse(e.data));
  // return ()=>ws.close();
  return () => {};
}

// -----------------------------
// UI components
// -----------------------------

function HelpTip({ text }: { text: string }) {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <span className="ml-2 inline-flex h-5 w-5 items-center justify-center rounded-full border text-xs text-muted-foreground">
            ?
          </span>
        </TooltipTrigger>
        <TooltipContent className="max-w-xs">
          <p className="text-sm">{text}</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}

function StatCard({ title, value, sub }: { title: string; value: React.ReactNode; sub?: string }) {
  return (
    <Card className="rounded-2xl shadow-sm">
      <CardHeader className="pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-semibold">{value}</div>
        {sub ? <div className="mt-1 text-xs text-muted-foreground">{sub}</div> : null}
      </CardContent>
    </Card>
  );
}

function CellTile({
  cell,
  target,
  isSelected,
  onSelect,
}: {
  cell: RemainderCell;
  target: string;
  isSelected: boolean;
  onSelect: (pos: number) => void;
}) {
  const color = pickColorForRemainder(cell.remainder, target);
  const factor = isFactorCell(cell);
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <button
            type="button"
            onClick={() => onSelect(cell.position)}
            className={`relative h-4 w-4 rounded-sm border transition-transform focus:outline-none focus:ring-2 focus:ring-offset-2 ${
              isSelected ? "ring-2 ring-offset-2" : ""
            }`}
            style={{ backgroundColor: color }}
            aria-label={`Cell position ${cell.position}, remainder ${cell.remainder}`}
          >
            {factor ? (
              <span className="absolute -right-1 -top-1 h-2 w-2 rounded-full" style={{ backgroundColor: "#ffffff" }} />
            ) : null}
          </button>
        </TooltipTrigger>
        <TooltipContent>
          <div className="space-y-1">
            <div className="text-xs">Position: <span className="font-medium">{cell.position}</span></div>
            <div className="text-xs">Remainder: <span className="font-medium">{cell.remainder}</span></div>
            {factor ? (
              <div className="text-xs font-medium" style={{ color: REMAINDER_COLORS.zero }}>
                Factor discovered
              </div>
            ) : null}
          </div>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}

function CellStateGrid({
  cells,
  target,
  arraySize,
  selectedPos,
  onSelectPos,
}: {
  cells: RemainderCell[];
  target: string;
  arraySize: number;
  selectedPos: number;
  onSelectPos: (pos: number) => void;
}) {
  // Adaptive grid sizing for up to 2000+ cells
  const cols = useMemo(() => {
    // Use ~50 columns on desktop, scale by array size.
    const base = 50;
    if (arraySize <= 400) return 25;
    if (arraySize <= 1000) return base;
    if (arraySize <= 2000) return 60;
    return 70;
  }, [arraySize]);

  const factorCells = useMemo(() => cells.filter(isFactorCell), [cells]);

  return (
    <Card className="rounded-2xl shadow-sm">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-3">
          <div>
            <CardTitle className="text-base">Cell State Grid</CardTitle>
            <div className="mt-1 text-xs text-muted-foreground">
              Final remainder states for positions 1..{formatInt(arraySize)} (primary view)
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary">R=0 is red</Badge>
            {factorCells.length ? (
              <Badge className="gap-1" variant="default">
                <CheckCircle2 className="h-3 w-3" /> {factorCells.length} factor cell(s)
              </Badge>
            ) : (
              <Badge variant="outline" className="gap-1">
                <AlertTriangle className="h-3 w-3" /> No factor cells
              </Badge>
            )}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="flex items-center justify-between gap-3">
          <div className="text-xs text-muted-foreground">
            Click a cell to inspect trajectory samples for that position.
          </div>
          <div className="text-xs">
            Selected position: <span className="font-medium">{selectedPos}</span>
          </div>
        </div>
        <div
          className="mt-3 grid gap-1"
          style={{ gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))` }}
          role="grid"
          aria-label="RemainderCell grid"
        >
          {cells.map((cell) => (
            <CellTile
              key={cell.position}
              cell={cell}
              target={target}
              isSelected={cell.position === selectedPos}
              onSelect={onSelectPos}
            />
          ))}
        </div>
        <Separator className="my-4" />
        <div className="flex flex-wrap items-center gap-3 text-xs">
          <div className="flex items-center gap-2">
            <span className="inline-block h-3 w-3 rounded-sm" style={{ backgroundColor: REMAINDER_COLORS.zero }} />
            <span>0 (factor found)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-block h-3 w-3 rounded-sm" style={{ backgroundColor: REMAINDER_COLORS.low }} />
            <span>low remainder</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-block h-3 w-3 rounded-sm" style={{ backgroundColor: REMAINDER_COLORS.med }} />
            <span>medium remainder</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-block h-3 w-3 rounded-sm" style={{ backgroundColor: REMAINDER_COLORS.high }} />
            <span>high remainder</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function TrialCard({
  trial,
  isActive,
  onSelect,
}: {
  trial: TrialResult;
  isActive: boolean;
  onSelect: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      className={`w-full text-left transition hover:opacity-95 focus:outline-none focus:ring-2 focus:ring-offset-2 ${
        isActive ? "" : ""
      }`}
    >
      <Card className={`rounded-2xl shadow-sm ${isActive ? "border-foreground/20" : ""}`}>
        <CardContent className="pt-5">
          <div className="flex items-start justify-between gap-3">
            <div>
              <div className="text-sm font-semibold">Trial {trial.trialIndex + 1}</div>
              <div className="mt-1 text-xs text-muted-foreground">
                Steps: {formatInt(trial.steps)} • Converged: {trial.converged ? "Yes" : "No"}
              </div>
            </div>
            <div className="flex items-center gap-2">
              {trial.factorFound ? (
                <Badge className="gap-1">
                  <CheckCircle2 className="h-3 w-3" /> Factor
                </Badge>
              ) : (
                <Badge variant="outline" className="gap-1">
                  <AlertTriangle className="h-3 w-3" /> No factor
                </Badge>
              )}
            </div>
          </div>
          {trial.factorFound && trial.factorPosition ? (
            <div className="mt-3 text-xs">
              Factor cell position: <span className="font-medium">{trial.factorPosition}</span>
            </div>
          ) : null}
        </CardContent>
      </Card>
    </button>
  );
}

function TrajectoryPanel({
  activeTrial,
  selectedPos,
  stepIndex,
  onStepIndex,
}: {
  activeTrial: TrialResult | null;
  selectedPos: number;
  stepIndex: number;
  onStepIndex: (i: number) => void;
}) {
  const steps = activeTrial?.trajectory ?? [];
  const maxIndex = Math.max(0, steps.length - 1);
  const idx = clamp(stepIndex, 0, maxIndex);
  const frame = steps[idx];

  const selectedSample = useMemo(() => {
    if (!frame) return null;
    return frame.samples.find((s) => s.position === selectedPos) || null;
  }, [frame, selectedPos]);

  return (
    <Card className="rounded-2xl shadow-sm">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-3">
          <div>
            <CardTitle className="text-base">Trajectory Panel</CardTitle>
            <div className="mt-1 text-xs text-muted-foreground">
              Step-wise remainder samples (per-trial). Slider enables frame-by-frame inspection.
            </div>
          </div>
          <Badge variant="secondary">Selected pos: {selectedPos}</Badge>
        </div>
      </CardHeader>
      <CardContent>
        {!activeTrial ? (
          <div className="text-sm text-muted-foreground">Run an experiment and select a trial to view trajectories.</div>
        ) : (
          <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="text-sm">
                Active: <span className="font-semibold">Trial {activeTrial.trialIndex + 1}</span>
              </div>
              <div className="text-xs text-muted-foreground">
                Showing {steps.length} frames • Final steps: {formatInt(activeTrial.steps)}
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span>Frame {idx + 1} / {steps.length}</span>
                <span>Step {frame?.step ?? 0}</span>
              </div>
              <Slider
                value={[idx]}
                min={0}
                max={maxIndex}
                step={1}
                onValueChange={(v) => onStepIndex(v[0] ?? 0)}
              />
            </div>

            <div className="grid gap-3 md:grid-cols-2">
              <Card className="rounded-2xl">
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">Selected Position Sample</CardTitle>
                </CardHeader>
                <CardContent>
                  {selectedSample ? (
                    <div className="space-y-2">
                      <div className="text-xs text-muted-foreground">Position {selectedSample.position}</div>
                      <div className="text-xl font-semibold">Remainder: {selectedSample.remainder}</div>
                      {selectedSample.remainder === "0" ? (
                        <div className="text-xs font-medium" style={{ color: REMAINDER_COLORS.zero }}>
                          Factor discovered at this frame
                        </div>
                      ) : null}
                    </div>
                  ) : (
                    <div className="text-sm text-muted-foreground">
                      No sample recorded for this position in the mock trajectory.
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card className="rounded-2xl">
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">ASCII Sparkline (Mock)</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-xs text-muted-foreground">
                    Quick visual of sampled remainders across frames. (Replace with real chart when wired.)
                  </div>
                  <div className="mt-3 rounded-xl border bg-muted/30 p-3 font-mono text-xs leading-5">
                    {renderAsciiSparkline(steps, selectedPos)}
                  </div>
                </CardContent>
              </Card>
            </div>

            <Card className="rounded-2xl">
              <CardHeader className="pb-2">
                <CardTitle className="text-sm">Frame Sample Table</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-2">
                  {(frame?.samples ?? []).slice(0, 12).map((s) => (
                    <div key={s.position} className="flex items-center justify-between rounded-xl border px-3 py-2">
                      <div className="text-xs">pos {s.position}</div>
                      <div className="text-xs font-mono">{s.remainder}</div>
                      {s.remainder === "0" ? (
                        <Badge className="gap-1">
                          <CheckCircle2 className="h-3 w-3" /> factor
                        </Badge>
                      ) : (
                        <Badge variant="secondary">r</Badge>
                      )}
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function renderAsciiSparkline(steps: NonNullable<TrialResult["trajectory"]>, pos: number) {
  if (!steps?.length) return "(no data)";
  // compress remainders to a 0..7 bucket to render ▁▂▃▄▅▆▇█
  const blocks = ["▁", "▂", "▃", "▄", "▅", "▆", "▇", "█"];
  const vals = steps.map((f) => f.samples.find((s) => s.position === pos)?.remainder ?? "0");
  const nums = vals.map((v) => (v === "0" ? 0 : hash32(v) % 100));
  const max = Math.max(1, ...nums);
  const line = nums
    .map((n) => {
      const b = Math.floor((7 * n) / max);
      return blocks[clamp(b, 0, 7)];
    })
    .join("");
  return line + "\n" + "0=" + blocks[0] + "  max=" + blocks[7] + "  (mock scale)";
}

// -----------------------------
// Main Dashboard
// -----------------------------

export default function FactorizationDashboard() {
  // Configuration (pre-populated defaults)
  const [semiprime, setSemiprime] = useState(DEFAULTS.semiprime);
  const [arraySize, setArraySize] = useState(String(DEFAULTS.arraySize));
  const [trials, setTrials] = useState(String(DEFAULTS.trials));
  const [maxSteps, setMaxSteps] = useState(String(DEFAULTS.maxSteps));
  const [stableSteps, setStableSteps] = useState(String(DEFAULTS.stableSteps));
  const [recordTrajectory, setRecordTrajectory] = useState<"on" | "off">("on");

  // Run state
  const [isRunning, setIsRunning] = useState(false);
  const [progressPct, setProgressPct] = useState(0);
  const [statusLine, setStatusLine] = useState("Idle");

  // Results
  const [trialResults, setTrialResults] = useState<TrialResult[]>([]);
  const [summary, setSummary] = useState<ExperimentSummary | null>(null);
  const [activeTrialIndex, setActiveTrialIndex] = useState<number>(0);
  const [selectedPos, setSelectedPos] = useState<number>(2);
  const [trajectoryStepIndex, setTrajectoryStepIndex] = useState<number>(0);

  const cfg = useMemo(() => {
    const as = clamp(safeInt(arraySize, DEFAULTS.arraySize), 2, 5000);
    const tr = clamp(safeInt(trials, DEFAULTS.trials), 1, 200);
    const ms = clamp(safeInt(maxSteps, DEFAULTS.maxSteps), 10, 5_000_000);
    const ss = clamp(safeInt(stableSteps, DEFAULTS.stableSteps), 1, 1000);
    return { semiprime: semiprime.trim(), arraySize: as, trials: tr, maxSteps: ms, stableSteps: ss };
  }, [semiprime, arraySize, trials, maxSteps, stableSteps]);

  const activeTrial = trialResults[activeTrialIndex] ?? null;

  const activeCells = activeTrial?.finalCells ?? [];

  const canRun = useMemo(() => {
    return cfg.semiprime.length > 0 && cfg.arraySize >= 2 && cfg.trials >= 1;
  }, [cfg]);

  async function runMockExperiment() {
    setIsRunning(true);
    setProgressPct(0);
    setStatusLine("Starting trials…");
    setTrialResults([]);
    setSummary(null);
    setActiveTrialIndex(0);
    setTrajectoryStepIndex(0);
    setSelectedPos((p) => clamp(p, 2, cfg.arraySize));

    // In real build, prefer WebSocket streaming for FR-CTL-02.
    // For mock: simulate progress and create deterministic trial payloads.
    const results: TrialResult[] = [];
    for (let t = 0; t < cfg.trials; t++) {
      setStatusLine(`Running trial ${t + 1} / ${cfg.trials}`);
      // simulate trial progress
      for (let k = 0; k <= 12; k++) {
        const pct = Math.round(((t + k / 12) / cfg.trials) * 100);
        setProgressPct(pct);
        // eslint-disable-next-line no-await-in-loop
        await new Promise((r) => setTimeout(r, 25));
      }
      const trial = buildMockTrial(t, {
        semiprime: cfg.semiprime,
        arraySize: cfg.arraySize,
        maxSteps: cfg.maxSteps,
        stableSteps: cfg.stableSteps,
      });
      // Drop trajectory if toggled off
      if (recordTrajectory === "off") delete (trial as any).trajectory;
      results.push(trial);
      setTrialResults([...results]);
      setActiveTrialIndex(0);
    }

    const s = summarize(results);
    setSummary(s);
    setStatusLine("Completed");
    setProgressPct(100);
    setIsRunning(false);
  }

  function loadMockCompleted() {
    // Post-hoc load: in real app this could select a saved JSON from backend.
    const preset = { ...DEFAULTS, semiprime: "100099", arraySize: 1000, trials: 5 };
    setSemiprime(preset.semiprime);
    setArraySize(String(preset.arraySize));
    setTrials(String(preset.trials));
    setMaxSteps(String(preset.maxSteps));
    setStableSteps(String(preset.stableSteps));

    const results = Array.from({ length: preset.trials }, (_, i) =>
      buildMockTrial(i, {
        semiprime: preset.semiprime,
        arraySize: preset.arraySize,
        maxSteps: preset.maxSteps,
        stableSteps: preset.stableSteps,
      })
    );
    setTrialResults(results);
    setSummary(summarize(results));
    setActiveTrialIndex(0);
    setSelectedPos(2);
    setTrajectoryStepIndex(0);
    setStatusLine("Loaded completed run (mock)");
    setProgressPct(100);
  }

  return (
    <div className="mx-auto w-full max-w-7xl space-y-6 p-4 md:p-6">
      <header className="space-y-2">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl font-semibold tracking-tight">Factorization Experiment Dashboard</h1>
            <p className="text-sm text-muted-foreground">
              Emergent Doom Engine • Mock UI for <span className="font-mono">FactorizationExperiment.java</span>
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary">Doc v1.0 • 2026-01-02</Badge>
            <Badge variant="outline">Ready for REST/WebSocket wiring</Badge>
          </div>
        </div>
      </header>

      <div className="grid gap-6 lg:grid-cols-12">
        {/* Configuration Panel */}
        <Card className="rounded-2xl shadow-sm lg:col-span-4">
          <CardHeader className="pb-3">
            <CardTitle className="text-base">Configuration</CardTitle>
            <div className="text-xs text-muted-foreground">
              Inputs are pre-populated with defaults and can be edited before run.
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-2">
              <div className="flex items-center">
                <Label htmlFor="semiprime">Target semiprime</Label>
                <HelpTip text="FR-CFG-01/06: Integer N to factor. Sent to backend as BigInteger string." />
              </div>
              <Input
                id="semiprime"
                value={semiprime}
                onChange={(e) => setSemiprime(e.target.value)}
                placeholder="e.g., 100099"
                inputMode="numeric"
              />
            </div>

            <div className="grid gap-2">
              <div className="flex items-center">
                <Label htmlFor="arraySize">Array size</Label>
                <HelpTip text="FR-CFG-02: Number of RemainderCells (positions 1..arraySize). Supports 2000+." />
              </div>
              <Input
                id="arraySize"
                value={arraySize}
                onChange={(e) => setArraySize(e.target.value)}
                placeholder="1000"
                inputMode="numeric"
              />
            </div>

            <div className="grid gap-2">
              <div className="flex items-center">
                <Label htmlFor="trials">Trials</Label>
                <HelpTip text="FR-CFG-03: Number of independent runs for statistics." />
              </div>
              <Input
                id="trials"
                value={trials}
                onChange={(e) => setTrials(e.target.value)}
                placeholder="5"
                inputMode="numeric"
              />
            </div>

            <div className="grid gap-2 md:grid-cols-2">
              <div className="grid gap-2">
                <div className="flex items-center">
                  <Label htmlFor="maxSteps">Max steps</Label>
                  <HelpTip text="FR-CFG-04: Upper bound to stop runaway experiments." />
                </div>
                <Input
                  id="maxSteps"
                  value={maxSteps}
                  onChange={(e) => setMaxSteps(e.target.value)}
                  placeholder="50000"
                  inputMode="numeric"
                />
              </div>

              <div className="grid gap-2">
                <div className="flex items-center">
                  <Label htmlFor="stableSteps">Stable steps</Label>
                  <HelpTip text="FR-CFG-05: Convergence threshold (no-change steps)." />
                </div>
                <Input
                  id="stableSteps"
                  value={stableSteps}
                  onChange={(e) => setStableSteps(e.target.value)}
                  placeholder="3"
                  inputMode="numeric"
                />
              </div>
            </div>

            <div className="grid gap-2">
              <div className="flex items-center">
                <Label>Trajectory recording</Label>
                <HelpTip text="FR-TRJ-01: If enabled, backend returns trajectory data for step-by-step inspection." />
              </div>
              <Select value={recordTrajectory} onValueChange={(v: any) => setRecordTrajectory(v)}>
                <SelectTrigger>
                  <SelectValue placeholder="Select" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="on">On</SelectItem>
                  <SelectItem value="off">Off</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-2 pt-2">
              <Button
                className="gap-2"
                onClick={runMockExperiment}
                disabled={!canRun || isRunning}
              >
                {isRunning ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" /> Running
                  </>
                ) : (
                  <>
                    <Play className="h-4 w-4" /> Run Experiment
                  </>
                )}
              </Button>

              <Button variant="outline" className="gap-2" onClick={loadMockCompleted} disabled={isRunning}>
                <Upload className="h-4 w-4" /> Load Completed Results (Mock)
              </Button>

              <div className="mt-2 space-y-2">
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                  <span>Status: {statusLine}</span>
                  <span>{progressPct}%</span>
                </div>
                <Progress value={progressPct} />
              </div>
            </div>

            <Separator />

            <div className="space-y-2 text-xs text-muted-foreground">
              <div className="font-medium text-foreground">Backend contracts (wire-ready)</div>
              <div className="rounded-xl border bg-muted/30 p-3 font-mono">
                POST /api/factorization/experiment/run\n
                ws://.../ws/experiment
              </div>
              <div>
                Factor found condition: <span className="font-mono">position &gt; 1</span> and <span className="font-mono">remainder == 0</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Main Panels */}
        <div className="space-y-6 lg:col-span-8">
          {/* Summary Metrics */}
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Factor found"
              value={summary ? (summary.factorFound ? "Yes" : "No") : "—"}
              sub="FR-MET-01"
            />
            <StatCard
              title="Mean steps"
              value={summary ? formatInt(summary.meanSteps) : "—"}
              sub="FR-MET-02"
            />
            <StatCard
              title="Convergence rate"
              value={summary ? `${summary.convergenceRatePct}%` : "—"}
              sub="FR-MET-03"
            />
            <StatCard
              title="Trials run"
              value={summary ? formatInt(summary.trialsRun) : "—"}
              sub="FR-MET-04"
            />
          </div>

          <Tabs defaultValue="grid" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="grid">Cell grid</TabsTrigger>
              <TabsTrigger value="traj">Trajectories</TabsTrigger>
              <TabsTrigger value="trials">Trials</TabsTrigger>
            </TabsList>

            {/* Cell State Grid */}
            <TabsContent value="grid" className="mt-4">
              <CellStateGrid
                cells={activeCells}
                target={cfg.semiprime}
                arraySize={cfg.arraySize}
                selectedPos={selectedPos}
                onSelectPos={(pos) => {
                  setSelectedPos(pos);
                  setTrajectoryStepIndex(0);
                }}
              />
            </TabsContent>

            {/* Trajectory Panel */}
            <TabsContent value="traj" className="mt-4">
              <TrajectoryPanel
                activeTrial={activeTrial}
                selectedPos={selectedPos}
                stepIndex={trajectoryStepIndex}
                onStepIndex={setTrajectoryStepIndex}
              />
            </TabsContent>

            {/* Trial Results List */}
            <TabsContent value="trials" className="mt-4">
              <Card className="rounded-2xl shadow-sm">
                <CardHeader className="pb-3">
                  <CardTitle className="text-base">Trial Results</CardTitle>
                  <div className="text-xs text-muted-foreground">
                    Select a trial to drive the grid and trajectory views.
                  </div>
                </CardHeader>
                <CardContent>
                  {trialResults.length === 0 ? (
                    <div className="text-sm text-muted-foreground">No results yet.</div>
                  ) : (
                    <div className="grid gap-3 md:grid-cols-2">
                      {trialResults.map((t) => (
                        <TrialCard
                          key={t.trialIndex}
                          trial={t}
                          isActive={t.trialIndex === activeTrialIndex}
                          onSelect={() => {
                            setActiveTrialIndex(t.trialIndex);
                            setTrajectoryStepIndex(0);
                          }}
                        />
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          <Card className="rounded-2xl shadow-sm">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Notes</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <div>
                This is a mock UI. It uses deterministic pseudo-data to demonstrate layout, interactions, and
                backend integration points.
              </div>
              <div>
                Primary requirement emphasis is respected: cell state grid first, then trajectories, then mean steps.
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
