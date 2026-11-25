public class Process {

    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int priorityWaitingSince = -1;

    private int remainingTime;

    private int firstStartTime;
    private int finishTime;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;

        this.remainingTime = burstTime;

        this.firstStartTime = -1;
        this.finishTime = -1;
    }

    public String getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getFirstStartTime() {
        return firstStartTime;
    }

    public void setFirstStartTime(int firstStartTime) {
        this.firstStartTime = firstStartTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public void enterPriorityQueue(int currentTime) {
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }
        if (priorityWaitingSince != -1) {
            throw new IllegalStateException(
                    id + " is already waiting in the Priority Queue.");
        }
        priorityWaitingSince = currentTime;
    }

    // Calculate how long the process has waited in Tier 2
    public int getPriorityWaitingTime(int currentTime) {
        if (currentTime < 0) {
            throw new IllegalArgumentException("Current time cannot be negative.");
        }
        if (priorityWaitingSince == -1) {
            return 0;
        }
        return Math.max(0, currentTime - priorityWaitingSince);
    }

    // Calculate the priority after applying Aging
    public int getEffectivePriority(int currentTime, int agingInterval) {
        if (agingInterval <= 0) {
            throw new IllegalArgumentException(
                    "Aging interval must be greater than zero.");
        }

        int waitingTime = getPriorityWaitingTime(currentTime);
        int agingLevels = waitingTime / agingInterval;

        return Math.max(1, priority - agingLevels);
    }

    // Stops counting waiting time when the process leaves the queue to run
    public void leavePriorityQueue() {
        priorityWaitingSince = -1;
    }
}