package ie3.i_e3_backend.model.DTOs;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;


public class ContractDTO {

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
    private Long users;

    private List<Long> profile;

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

    public Long getUsers() {
        return users;
    }

    public void setUsers(final Long users) {
        this.users = users;
    }

    public List<Long> getProfile() {
        return profile;
    }

    public void setProfile(final List<Long> profile) {
        this.profile = profile;
    }

}
