package ie3.i_e3_backend.model.DTOs;

public class ProjectCountStatusDTO {
    private int totalProjectCount;

    private int completedProjectCount;
    private int availableProjectCount;
    private int plannedProjectCount;
    private int finishedProjectCount;
    private int unavailableProjectCount;

    public int getTotalProjectCount() {
        return totalProjectCount;
    }

    public void setTotalProjectCount(int totalProjectCount) {
        this.totalProjectCount = totalProjectCount;
    }

    public int getCompletedProjectCount() {
        return completedProjectCount;
    }

    public void setCompletedProjectCount(int completedProjectCount) {
        this.completedProjectCount = completedProjectCount;
    }

    public int getPlannedProjectCount() {
        return plannedProjectCount;
    }

    public void setPlannedProjectCount(int plannedProjectCount) {
        this.plannedProjectCount = plannedProjectCount;
    }

    public int getAvailableProjectCount() {
        return availableProjectCount;
    }

    public void setAvailableProjectCount(int availableProjectCount) {
        this.availableProjectCount = availableProjectCount;
    }

    public int getUnavailableProjectCount() {
        return unavailableProjectCount;
    }

    public void setUnavailableProjectCount(int unavailableProjectCount) {
        this.unavailableProjectCount = unavailableProjectCount;
    }

    public int getFinishedProjectCount() {
        return finishedProjectCount;
    }

    public void setFinishedProjectCount(int finishedProjectCount) {
        this.finishedProjectCount = finishedProjectCount;
    }
}
