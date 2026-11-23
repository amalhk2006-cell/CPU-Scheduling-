import java.util.ArrayList;
import java.util.List;
public class SRTF{
        private List<String> timeline;
        private int contextSwitches;
        public SRTF() {
            timeline = new ArrayList<>();
            contextSwitches = 0;
        }
        public void schedule(List<Process> processes, int contextSwitchTime) {
            timeline.clear();
            contextSwitches = 0;
            int currentTime = 0;
            int completed = 0;
            Process previousProcess = null;
            while (completed < processes.size()) {
                Process currentProcess = getShortestRemainingProcess(
                        processes, currentTime
                );
                // No process is available
                if (currentProcess == null) {
                    timeline.add("IDLE");
                    currentTime++;
                    previousProcess = null;
                    continue;
                }

                // Context switch between two different processes
                if (previousProcess != null &&
                        previousProcess != currentProcess) {

                    contextSwitches++;

                    for (int i = 0; i < contextSwitchTime; i++) {
                        timeline.add("CS");
                        currentTime++;
                    }

                    previousProcess = null;
                    continue;
                }

                // Record the first time this process gets the CPU
                if (currentProcess.getFirstStartTime() == -1) {
                    currentProcess.setFirstStartTime(currentTime);
                }

                // Execute for one time unit
                timeline.add(currentProcess.getId());

                currentProcess.setRemainingTime(
                        currentProcess.getRemainingTime() - 1
                );

                currentTime++;

                // Check if the process has finished
                if (currentProcess.getRemainingTime() == 0) {
                    currentProcess.setFinishTime(currentTime);
                    completed++;
                }

                previousProcess = currentProcess;
            }
        }

        private Process getShortestRemainingProcess(
                List<Process> processes, int currentTime) {

            Process shortest = null;

            for (Process process : processes) {

                if (process.getArrivalTime() <= currentTime &&
                        process.getRemainingTime() > 0) {

                    if (shortest == null ||
                            process.getRemainingTime() <
                                    shortest.getRemainingTime()) {

                        shortest = process;
                    }
                }
            }

            return shortest;
        }

        public List<String> getTimeline() {
            return timeline;
        }

        public int getContextSwitches() {
            return contextSwitches;
        }
}