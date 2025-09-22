package ie3.i_e3_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "Contracts")
public class Contract {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime startDate;

    @Column(nullable = false)
    private OffsetDateTime endDate;

    @Column(nullable = false)
    private Long weeklyHours;

    @Column(nullable = false)
    private Double wageByHour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Employee users;

    @ManyToMany
    @JoinTable(
            name = "EmployeeRoles",
            joinColumns = @JoinColumn(name = "contractId"),
            inverseJoinColumns = @JoinColumn(name = "profileId")
    )
    private Set<Profile> profile = new HashSet<>();

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

    public Employee getUsers() {
        return users;
    }

    public void setUsers(final Employee users) {
        this.users = users;
    }

    public Set<Profile> getProfile() {
        return profile;
    }

    public void setProfile(final Set<Profile> profile) {
        this.profile = profile;
    }

}
