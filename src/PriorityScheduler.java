import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PriorityScheduler {

    public static final int DEFAULT_AGING_INTERVAL = 4;

    private final List<Process> readyQueue;
    private final int agingInterval;

    public PriorityScheduler() {
        this(DEFAULT_AGING_INTERVAL);
    }

    public PriorityScheduler(int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException(
                    "Aging interval must be greater than zero.");
        }

        this.agingInterval = agingInterval;
        this.readyQueue = new ArrayList<>();
    }

    public void addProcess(Process process, int currentTime) {
        if (process == null) {
            throw new IllegalArgumentException("Process cannot be null.");
        }
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }
        if (process.getRemainingTime() <= 0) {
            return;
        }
        if (readyQueue.contains(process)) {
            throw new IllegalArgumentException(
                    process.getId() + " is already in the Priority Queue.");
        }

        process.enterPriorityQueue(currentTime);
        readyQueue.add(process);
    }

    public boolean hasReadyProcesses() {
        return !readyQueue.isEmpty();
    }

    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    public Process selectNextProcess(int currentTime) {

        if (readyQueue.isEmpty()) {
            return null;
        }

        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        Process selected = Collections.min(
                readyQueue,
                Comparator.comparingInt(
                        (Process process) -> process.getEffectivePriority(
                                currentTime,
                                agingInterval))
                        .thenComparingInt(Process::getArrivalTime));

        readyQueue.remove(selected);
        selected.leavePriorityQueue();

        return selected;
    }

    /** Returns the process that would be selected, without removing it. */
    public Process peekNextProcess(int currentTime) {
        if (readyQueue.isEmpty()) return null;
        return Collections.min(readyQueue,
                Comparator.comparingInt((Process process) ->
                        process.getEffectivePriority(currentTime, agingInterval))
                        .thenComparingInt(Process::getArrivalTime));
    }

    public ExecutionResult executeNextProcess(int currentTime) {
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }

        Process currentProcess = selectNextProcess(currentTime);

        if (currentProcess == null) {
            return null;
        }

        if (currentProcess.getFirstStartTime() == -1) {
            currentProcess.setFirstStartTime(currentTime);
        }

        int executionTime = currentProcess.getRemainingTime();
        int endTime = currentTime + executionTime;

        currentProcess.setRemainingTime(0);
        currentProcess.setFinishTime(endTime);

        return new ExecutionResult(
                currentProcess,
                currentTime,
                endTime,
                executionTime);
    }

    public List<Process> getReadyQueueSnapshot() {
        return Collections.unmodifiableList(
                new ArrayList<>(readyQueue));
    }

    public static class ExecutionResult {

        private final Process process;
        private final int startTime;
        private final int endTime;
        private final int executionTime;

        public ExecutionResult(
                Process process,
                int startTime,
                int endTime,
                int executionTime) {
            this.process = process;
            this.startTime = startTime;
            this.endTime = endTime;
            this.executionTime = executionTime;
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
    }
}
