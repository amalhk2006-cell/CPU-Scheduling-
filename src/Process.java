public class Process {

    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;

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
}