package ie3.i_e3_backend.model.DTOs;

public class ProjectCostDTO {

    private double totalCost;
    private double totalCostPerPeriod;

    public double getTotalCost() { return totalCost; }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalCostPerPeriod() {
        return totalCostPerPeriod;
    }

    public void setTotalCostPerPeriod(double totalCostPerPeriod) {
        this.totalCostPerPeriod = totalCostPerPeriod;
    }
}
