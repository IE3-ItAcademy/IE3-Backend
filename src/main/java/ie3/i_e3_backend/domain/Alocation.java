package ie3.i_e3_backend.domain;

import ie3.i_e3_backend.model.Enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


@Entity
@Table(name = "Alocations")
public class Alocation {

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
    private Integer weeklyHours;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role employeeRole;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private Employee user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", unique = true)
    private Project project;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(final Integer weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Role getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(final Role employeeRole) {
        this.employeeRole = employeeRole;
    }

    public Employee getUser() {
        return user;
    }

    public void setUser(final Employee user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

}
