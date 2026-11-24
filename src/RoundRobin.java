import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Implements ACS Tier 1 (Round Robin) one time-quantum at a time.
 *
 * The ACS controller is responsible for:
 *  - calling addArrivedProcesses(...) as simulated time advances;
 *  - choosing whether Tier 1 or Tier 2 should run next;
 *  - accounting for context-switching time exactly once.
 *
 * A process that finishes in its quantum is recorded as completed.
 * A process that still has remaining time after its quantum is demoted
 * and is NOT inserted back into the Tier 1 queue.
 */
public class RoundRobin {

    private final Queue<Process> readyQueue;
    private final Set<Process> admittedProcesses;
    private final List<Process> completedProcesses;
    private final List<Process> demotedProcesses;
    private final int quantum;

    public RoundRobin(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than zero.");
        }

        this.quantum = quantum;
        this.readyQueue = new ArrayDeque<>();
        this.admittedProcesses = Collections.newSetFromMap(new IdentityHashMap<>());
        this.completedProcesses = new ArrayList<>();
        this.demotedProcesses = new ArrayList<>();
    }

    /**
     * Adds every process that has arrived by currentTime to Tier 1.
     *
     * ACS should call this before selecting the next process. Processes are
     * inserted in ascending arrival-time order. If arrival times are equal,
     * their original order in allProcesses is preserved.
     * Already-admitted and already-completed processes are not added again.
     *
     * @return the number of newly admitted processes.
     */
    public int addArrivedProcesses(List<Process> allProcesses, int currentTime) {
        Objects.requireNonNull(allProcesses, "Process list cannot be null.");

        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        List<Process> newlyArrived = new ArrayList<>();

        for (Process process : allProcesses) {
            Objects.requireNonNull(process, "Process list cannot contain null.");

            if (process.getArrivalTime() <= currentTime
                    && process.getRemainingTime() > 0
                    && admittedProcesses.add(process)) {
                newlyArrived.add(process);
            }
        }

        // Stable sorting preserves input order when arrival times are equal.
        newlyArrived.sort(Comparator.comparingInt(Process::getArrivalTime));

        for (Process process : newlyArrived) {
            readyQueue.offer(process);
        }

        return newlyArrived.size();
    }

    /** Returns true when at least one process is waiting in Tier 1. */
    public boolean hasReadyProcesses() {
        return !readyQueue.isEmpty();
    }

    /** Returns the number of processes currently waiting in Tier 1. */
    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    /** Returns the configured static time quantum. */
    public int getQuantum() {
        return quantum;
    }

    /**
     * Executes exactly one Round Robin turn (one quantum or less).
     *
     * @param currentTime the CPU time at which this process starts running;
     *                    ACS should pass the time after any required context switch.
     * @return details of the execution, or null if Tier 1 is empty.
     */
    public ExecutionResult executeNextQuantum(int currentTime) {
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        Process currentProcess = readyQueue.poll();
        if (currentProcess == null) {
            return null;
        }

        if (currentProcess.getArrivalTime() > currentTime) {
            // Restore it so an incorrect early dispatch does not lose the process.
            readyQueue.offer(currentProcess);
            throw new IllegalStateException(
                    currentProcess.getId() + " has not arrived yet. Arrival time: "
                            + currentProcess.getArrivalTime() + ", current time: " + currentTime
            );
        }

        int remainingBeforeExecution = currentProcess.getRemainingTime();
        if (remainingBeforeExecution <= 0) {
            throw new IllegalStateException(
                    "A process with no remaining burst time was found in Tier 1: "
                            + currentProcess.getId()
            );
        }

        // Record the first CPU start only once. Do not overwrite it later.
        if (currentProcess.getFirstStartTime() == -1) {
            currentProcess.setFirstStartTime(currentTime);
        }

        // Run for the smaller of the remaining burst time and the time quantum.
        int executionTime = Math.min(quantum, remainingBeforeExecution);
        int endTime = currentTime + executionTime;

        currentProcess.setRemainingTime(remainingBeforeExecution - executionTime);

        boolean completed = currentProcess.getRemainingTime() == 0;

        if (completed) {
            currentProcess.setFinishTime(endTime);
            completedProcesses.add(currentProcess);
        } else {
            // Assignment rule: demote it to Tier 2; do not re-queue it in Tier 1.
            demotedProcesses.add(currentProcess);
        }

        return new ExecutionResult(
                currentProcess,
                currentTime,
                endTime,
                executionTime,
                completed
        );
    }

    /** A read-only copy of the processes still waiting in Tier 1. */
    public List<Process> getReadyQueueSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(readyQueue));
    }

    /** A read-only copy of processes that completed in Tier 1. */
    public List<Process> getCompletedProcesses() {
        return Collections.unmodifiableList(new ArrayList<>(completedProcesses));
    }

    /** A read-only copy of processes that must be transferred to Tier 2. */
    public List<Process> getDemotedProcesses() {
        return Collections.unmodifiableList(new ArrayList<>(demotedProcesses));
    }

    /** Result of running one process for one Round Robin turn. */
    public static class ExecutionResult {
        private final Process process;
        private final int startTime;
        private final int endTime;
        private final int executionTime;
        private final boolean completed;

        private ExecutionResult(
                Process process,
                int startTime,
                int endTime,
                int executionTime,
                boolean completed
        ) {
            this.process = process;
            this.startTime = startTime;
            this.endTime = endTime;
            this.executionTime = executionTime;
            this.completed = completed;
        }

        public Process getProcess() {
            return process;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public int getExecutionTime() {
            return executionTime;
        }

        public boolean isCompleted() {
            return completed;
        }

        /** True when the process still has work after using its quantum. */
        public boolean isDemoted() {
            return !completed;
        }
    }
}
