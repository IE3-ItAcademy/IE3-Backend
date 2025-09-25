package ie3.i_e3_backend.model.DTOs;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;


public class ContractReadDTO {

    private Long id;

    @NotNull
    private OffsetDateTime startDate;

    @NotNull
    private OffsetDateTime endDate;

    @NotNull
    private Long weeklyHours;

    @NotNull
    private Double wageByHour;

    @NotNull
    private Long employeeId;

    @NotNull
    private String employeeName;

    private List<Long> profile;

    private boolean activeContract;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(final OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(final OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(final Long weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Double getWageByHour() {
        return wageByHour;
    }

    public void setWageByHour(final Double wageByHour) {
        this.wageByHour = wageByHour;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(final String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getEmployeeId() { return employeeId; }

    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public List<Long> getProfile() {
        return profile;
    }

    public void setProfile(final List<Long> profile) {
        this.profile = profile;
    }

    public boolean isActiveContract() {
        return activeContract;
    }

    public void setActiveContract(boolean activeContract) {
        this.activeContract = activeContract;
    }
}
