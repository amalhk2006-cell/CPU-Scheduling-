import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PriorityScheduler {

    private final List<Process> readyQueue;  

    public PriorityScheduler() {
        readyQueue = new ArrayList<>();
    }

    public void addProcess(Process process) {
      if (process != null && process.getRemainingTime() > 0) {
        readyQueue.add(process);
      }
    }

    public boolean hasReadyProcesses() {
        return !readyQueue.isEmpty();
    }

    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    public Process selectNextProcess() {

        if (readyQueue.isEmpty()) {
            return null;
        }

        Process selected = Collections.min(
                readyQueue,
                Comparator.comparingInt(Process::getPriority)
                        .thenComparingInt(Process::getArrivalTime)
        );

        readyQueue.remove(selected);

        return selected;
    }

    public ExecutionResult executeNextProcess(int currentTime) {

        Process currentProcess = selectNextProcess();

        if (currentProcess == null) {
            return null;
        }

        int executionTime = currentProcess.getRemainingTime();

        int endTime = currentTime + executionTime;

        currentProcess.setRemainingTime(0);

        currentProcess.setFinishTime(endTime);

        return new ExecutionResult(
                currentProcess,
                currentTime,
                endTime,
                executionTime
        );
    }

    public List<Process> getReadyQueueSnapshot() {
        return Collections.unmodifiableList(
                new ArrayList<>(readyQueue)
        );
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
                int executionTime
        ) {
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

