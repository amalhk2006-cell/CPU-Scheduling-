import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adaptive Container Scheduler (ACS).
 * Tier 1: Round Robin.
 * Tier 2: Non-preemptive Priority with Aging.
 */

public class ACS {

    private final int quantum;
    private final int contextSwitchTime;
    private final int agingInterval;

    private RoundRobin tier1;
    private PriorityScheduler tier2;

    private final List<String> timeline = new ArrayList<>();
    private final List<String> demotionMatrix = new ArrayList<>();
    private int contextSwitches;
    private int finishTime;

    public ACS(int quantum, int contextSwitchTime) {
        this(quantum, contextSwitchTime, PriorityScheduler.DEFAULT_AGING_INTERVAL);
    }

    public ACS(int quantum, int contextSwitchTime, int agingInterval) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero.");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }
        if (agingInterval <= 0) {
            throw new IllegalArgumentException("Aging interval must be greater than zero.");
        }

        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
        this.agingInterval = agingInterval;
    }

    /** Runs ACS. Use fresh Process objects for every separate experiment. */
    public void schedule(List<Process> processes) {
        Objects.requireNonNull(processes, "Process list cannot be null.");
        resetResults();

        for (Process process : processes) {
            Objects.requireNonNull(process, "Process list cannot contain null.");
            if (process.getArrivalTime() < 0 || process.getBurstTime() <= 0) {
                throw new IllegalArgumentException("Invalid arrival or burst time for " + process.getId());
            }
            if (process.getRemainingTime() != process.getBurstTime()
                    || process.getFirstStartTime() != -1 || process.getFinishTime() != -1) {
                throw new IllegalArgumentException(
                        "Use fresh Process objects for each ACS run. Invalid state: " + process.getId());
            }
        }

        tier1 = new RoundRobin(quantum);
        tier2 = new PriorityScheduler(agingInterval);

        int currentTime = 0;
        String lastProcessId = null;

        while (!allProcessesCompleted(processes)) {
            tier1.addArrivedProcesses(processes, currentTime);

            // If both queues are empty, advance to the next arrival.
            if (!tier1.hasReadyProcesses() && !tier2.hasReadyProcesses()) {
                int nextArrival = findNextArrival(processes, currentTime);
                if (nextArrival == Integer.MAX_VALUE) {
                    break;
                }

                while (currentTime < nextArrival) {
                    timeline.add("IDLE");
                    recordState(currentTime, "IDLE", -1);
                    currentTime++;
                }
                lastProcessId = null; // No context switch after an idle period.
                continue;
            }

            // Charge a context switch only when the next process differs from
            // the previous one. A demoted process may immediately continue in Tier 2.
            Process nextProcess = tier1.hasReadyProcesses()
                    ? tier1.peekNextProcess()
                    : tier2.peekNextProcess(currentTime);

            if (lastProcessId != null && nextProcess != null
                    && !lastProcessId.equals(nextProcess.getId())) {
                contextSwitches++;
                for (int i = 0; i < contextSwitchTime; i++) {
                    tier1.addArrivedProcesses(processes, currentTime);
                    timeline.add("CS");
                    recordState(currentTime, "CS", -1);
                    currentTime++;
                }
                tier1.addArrivedProcesses(processes, currentTime);
            }

            // Re-check after context switching because new Tier 1 processes may have arrived.
            if (tier1.hasReadyProcesses()) {
                RoundRobin.ExecutionResult result = tier1.executeNextQuantum(currentTime);
                Process running = result.getProcess();
                int remainingBefore = running.getRemainingTime() + result.getExecutionTime();

                // Record every time unit of this RR turn. New arrivals join Tier 1
                // at their actual arrival times while the current process keeps the CPU.
                for (int t = result.getStartTime(); t < result.getEndTime(); t++) {
                    if (t > result.getStartTime()) {
                        tier1.addArrivedProcesses(processes, t);
                    }
                    int remainingAtStartOfUnit = remainingBefore - (t - result.getStartTime());
                    timeline.add(running.getId());
                    recordState(t, running.getId(), remainingAtStartOfUnit);
                }

                currentTime = result.getEndTime();
                if (result.isDemoted()) {
                    tier2.addProcess(running, currentTime);
                }
                lastProcessId = running.getId();

            } else {
                // Tier 1 is empty, so Tier 2 may execute.
                PriorityScheduler.ExecutionResult result = executeTier2OneProcess(currentTime, processes);
                if (result == null) {
                    continue;
                }
                currentTime = result.getEndTime();
                lastProcessId = result.getProcess().getId();
            }
        }

        finishTime = currentTime;
        tier1.addArrivedProcesses(processes, currentTime);
        recordState(currentTime, "IDLE", -1);
    }

    /** Executes one selected Tier 2 process without preemption, recording each time step. */
    private PriorityScheduler.ExecutionResult executeTier2OneProcess(
            int currentTime, List<Process> allProcesses) {

        Process process = tier2.selectNextProcess(currentTime);
        if (process == null) {
            return null;
        }

        if (process.getFirstStartTime() == -1) {
            process.setFirstStartTime(currentTime);
        }

        int startTime = currentTime;
        int executionTime = process.getRemainingTime();

        // The process is non-preemptive: it runs to completion. New arrivals
        // are admitted to Tier 1 during this interval but cannot interrupt it.
        for (int t = startTime; t < startTime + executionTime; t++) {
            tier1.addArrivedProcesses(allProcesses, t);
            int remainingAtStartOfUnit = executionTime - (t - startTime);
            timeline.add(process.getId());
            recordState(t, process.getId(), remainingAtStartOfUnit);
        }

        int endTime = startTime + executionTime;
        process.setRemainingTime(0);
        process.setFinishTime(endTime);

        return new PriorityScheduler.ExecutionResult(
                process, startTime, endTime, executionTime);
    }

    private void recordState(int time, String running, int runningRemaining) {
        String runningText = running;
        if (runningRemaining >= 0) {
            runningText += " (remaining: " + runningRemaining + ")";
        }

        demotionMatrix.add(
                "Time " + time
                        + " | Running: " + runningText
                        + " | Tier 1: " + formatQueue(tier1.getReadyQueueSnapshot(), time, false)
                        + " | Tier 2: " + formatQueue(tier2.getReadyQueueSnapshot(), time, true)
        );
    }

    private String formatQueue(List<Process> queue, int currentTime, boolean applyAging) {
        if (queue.isEmpty()) {
            return "[Empty]";
        }

        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < queue.size(); i++) {
            Process process = queue.get(i);
            if (i > 0) {
                result.append(", ");
            }

            int shownPriority = applyAging
                    ? process.getEffectivePriority(currentTime, agingInterval)
                    : process.getPriority();

            result.append(process.getId())
                    .append(" (remaining: ").append(process.getRemainingTime())
                    .append(", Priority: ").append(shownPriority).append(")");
        }
        return result.append("]").toString();
    }

    private int findNextArrival(List<Process> processes, int currentTime) {
        int nextArrival = Integer.MAX_VALUE;
        for (Process process : processes) {
            if (process.getRemainingTime() > 0 && process.getArrivalTime() > currentTime) {
                nextArrival = Math.min(nextArrival, process.getArrivalTime());
            }
        }
        return nextArrival;
    }

    private boolean allProcessesCompleted(List<Process> processes) {
        for (Process process : processes) {
            if (process.getRemainingTime() > 0) {
                return false;
            }
        }
        return true;
    }

    private void resetResults() {
        timeline.clear();
        demotionMatrix.clear();
        contextSwitches = 0;
        finishTime = 0;
    }

    public List<String> getTimeline() {
        return new ArrayList<>(timeline);
    }

    public List<String> getDemotionMatrix() {
        return new ArrayList<>(demotionMatrix);
    }

    public int getContextSwitches() {
        return contextSwitches;
    }

    public int getFinishTime() {
        return finishTime;
    }
}
