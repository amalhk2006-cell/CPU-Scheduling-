import java.util.List;

/** Prints timelines, per-process metrics, averages, and ACS queue-state history. */
public class ResultsManager {

    public static void printSRTFResults(List<Process> processes, SRTF scheduler) {
        printResults("SRTF", processes, scheduler.getTimeline(), scheduler.getContextSwitches());
    }

    public static void printACSResults(List<Process> processes, ACS scheduler) {
        printResults("ACS", processes, scheduler.getTimeline(), scheduler.getContextSwitches());
        System.out.println("\nACS Demotion / Queue-State Matrix:");
        for (String state : scheduler.getDemotionMatrix()) System.out.println(state);
        System.out.println("ACS Finish Time: " + scheduler.getFinishTime());
    }

    public static void printResults(String title, List<Process> processes,
                                    List<String> timeline, int contextSwitches) {
        List<Metrics.ProcessMetrics> metrics = Metrics.calculateAll(processes);
        System.out.println("\n========== " + title + " RESULTS ==========");
        System.out.println("Timeline:");
        for (int i = 0; i < timeline.size(); i++) {
            System.out.println(i + " - " + timeline.get(i));
        }

        System.out.println("\nProcess Metrics:");
        System.out.printf("%-10s %-10s %-12s %-12s %-12s%n",
                "Process", "Arrival", "Waiting", "Turnaround", "Response");
        for (Metrics.ProcessMetrics m : metrics) {
            Process p = m.getProcess();
            System.out.printf("%-10s %-10d %-12d %-12d %-12d%n",
                    p.getId(), p.getArrivalTime(), m.getWaitingTime(),
                    m.getTurnaroundTime(), m.getResponseTime());
        }

        System.out.printf("\nAverage Waiting Time: %.2f%n", Metrics.averageWaitingTime(processes));
        System.out.printf("Average Turnaround Time: %.2f%n", Metrics.averageTurnaroundTime(processes));
        System.out.printf("Average Response Time: %.2f%n", Metrics.averageResponseTime(processes));
        System.out.println("Context Switches: " + contextSwitches);
    }
}
