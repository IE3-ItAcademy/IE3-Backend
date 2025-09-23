package ie3.i_e3_backend.model.DTOs;

import java.time.OffsetDateTime;

public class ProjectCostDTO {

    private double totalCost;
    private double totalCostPerPeriod;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalCostPerPeriod() {
        return totalCostPerPeriod;
    }

    public void setTotalCostPerPeriod(double totalCostPerPeriod) {
        this.totalCostPerPeriod = totalCostPerPeriod;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }
}
