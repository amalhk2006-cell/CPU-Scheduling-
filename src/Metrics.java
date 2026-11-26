import java.util.ArrayList;
import java.util.List;

/** Calculates scheduling metrics after a scheduler has finished. */
public class Metrics {

    public static class ProcessMetrics {
        private final Process process;
        private final int waitingTime;
        private final int turnaroundTime;
        private final int responseTime;

        public ProcessMetrics(Process process) {
            if (process == null || process.getFinishTime() < 0
                    || process.getFirstStartTime() < 0) {
                throw new IllegalArgumentException("Process has not completed scheduling.");
            }
            this.process = process;
            this.turnaroundTime = process.getFinishTime() - process.getArrivalTime();
            this.waitingTime = turnaroundTime - process.getBurstTime();
            this.responseTime = process.getFirstStartTime() - process.getArrivalTime();
        }

        public Process getProcess() { return process; }
        public int getWaitingTime() { return waitingTime; }
        public int getTurnaroundTime() { return turnaroundTime; }
        public int getResponseTime() { return responseTime; }
    }

    public static List<ProcessMetrics> calculateAll(List<Process> processes) {
        if (processes == null) throw new IllegalArgumentException("Process list cannot be null.");
        List<ProcessMetrics> result = new ArrayList<>();
        for (Process process : processes) result.add(new ProcessMetrics(process));
        return result;
    }

    public static double averageWaitingTime(List<Process> processes) {
        return average(calculateAll(processes), 0);
    }

    public static double averageTurnaroundTime(List<Process> processes) {
        return average(calculateAll(processes), 1);
    }

    public static double averageResponseTime(List<Process> processes) {
        return average(calculateAll(processes), 2);
    }

    private static double average(List<ProcessMetrics> metrics, int type) {
        if (metrics.isEmpty()) return 0.0;
        double total = 0;
        for (ProcessMetrics m : metrics) {
            if (type == 0) total += m.getWaitingTime();
            else if (type == 1) total += m.getTurnaroundTime();
            else total += m.getResponseTime();
        }
        return total / metrics.size();
    }
}
