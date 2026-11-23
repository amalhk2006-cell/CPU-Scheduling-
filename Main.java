import java.util.ArrayList;
import java.util.List;
public class Main {
    public static void main(String[] args) {

        List<Process> processes = new ArrayList<>();

        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 1, 7, 3));
        processes.add(new Process("P3", 3, 2, 1));

        SRTF scheduler = new SRTF();

        scheduler.schedule(processes, 0);

        System.out.println("SRTF Timeline:");

        List<String> timeline = scheduler.getTimeline();

        for (int i = 0; i < timeline.size(); i++) {
            System.out.println(i + " - " + timeline.get(i));
        }

        System.out.println("\nProcess Results:");
        for (Process p : processes) {
            System.out.println(
                    p.getId() +
                            " | First Start: " + p.getFirstStartTime() +
                            " | Finish: " + p.getFinishTime()
            );
        }

        System.out.println(
                "\nContext Switches: " +
                        scheduler.getContextSwitches()
        );
    }
}
