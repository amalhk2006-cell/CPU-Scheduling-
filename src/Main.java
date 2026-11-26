
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n========== CPU SCHEDULING SIMULATOR ==========");
            System.out.println("1. SRTF");
            System.out.println("2. Adaptive Container Scheduler (ACS)");
            System.out.println("3. Run Both");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = readInt();

            if (choice == 4) {
                System.out.println("Exiting program...");
                break;
            }

            if (choice < 1 || choice > 4) {
                System.out.println("Invalid option. Try again.");
                continue;
            }

            // Read processes
            System.out.print("\nEnter number of processes: ");
            int n = readPositiveInt();

            String[] ids = new String[n];
            int[] arrivals = new int[n];
            int[] bursts = new int[n];
            int[] priorities = new int[n];

            for (int i = 0; i < n; i++) {

                System.out.println("\nProcess P" + (i + 1));

                ids[i] = "P" + (i + 1);

                System.out.print("Arrival Time: ");
                arrivals[i] = readNonNegativeInt();

                System.out.print("Burst Time: ");
                bursts[i] = readPositiveInt();

                System.out.print("Priority: ");
                priorities[i] = readNonNegativeInt();
            }

            // Read common configuration
            System.out.print("\nContext Switch Time: ");
            int contextSwitch = readNonNegativeInt();

            int quantum = 0;
            int agingInterval = 0;

            if (choice == 2 || choice == 3) {

                System.out.print("Time Quantum: ");
                quantum = readPositiveInt();

                System.out.print("Aging Interval: ");
                agingInterval = readPositiveInt();
            }

            // Run selected algorithm
            if (choice == 1) {
                runSRTF(ids, arrivals, bursts, priorities, contextSwitch);
            }
            else if (choice == 2) {
                runACS(ids, arrivals, bursts, priorities,
                       quantum, contextSwitch, agingInterval);
            }
            else {
                runSRTF(ids, arrivals, bursts, priorities, contextSwitch);

                runACS(ids, arrivals, bursts, priorities,
                       quantum, contextSwitch, agingInterval);
            }
        }

        input.close();
    }

    private static List<Process> createProcesses(
            String[] ids,
            int[] arrivals,
            int[] bursts,
            int[] priorities) {

        List<Process> processes = new ArrayList<>();

        for (int i = 0; i < ids.length; i++) {
            processes.add(new Process(
                    ids[i],
                    arrivals[i],
                    bursts[i],
                    priorities[i]
            ));
        }

        return processes;
    }

    private static void runSRTF(
            String[] ids,
            int[] arrivals,
            int[] bursts,
            int[] priorities,
            int contextSwitch) {

        System.out.println("\n========== RUNNING SRTF ==========");

        List<Process> processes =
                createProcesses(ids, arrivals, bursts, priorities);

        SRTF srtf = new SRTF();

        srtf.schedule(processes, contextSwitch);

        ResultsManager.printSRTFResults(processes, srtf);
    }

    private static void runACS(
            String[] ids,
            int[] arrivals,
            int[] bursts,
            int[] priorities,
            int quantum,
            int contextSwitch,
            int agingInterval) {

        System.out.println("\n========== RUNNING ACS ==========");

        List<Process> processes =
                createProcesses(ids, arrivals, bursts, priorities);

        ACS acs = new ACS(quantum, contextSwitch, agingInterval);

        acs.schedule(processes);

        ResultsManager.printACSResults(processes, acs);
    }

    private static int readInt() {
        while (!input.hasNextInt()) {
            System.out.print("Please enter a valid integer: ");
            input.next();
        }
        return input.nextInt();
    }

    private static int readPositiveInt() {
        int value;

        do {
            value = readInt();

            if (value <= 0) {
                System.out.print("Enter a number greater than zero: ");
            }

        } while (value <= 0);

        return value;
    }

    private static int readNonNegativeInt() {
        int value;

        do {
            value = readInt();

            if (value < 0) {
                System.out.print("Enter zero or a positive number: ");
            }

        } while (value < 0);

        return value;
    }
}
