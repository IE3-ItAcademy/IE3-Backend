package ie3.i_e3_backend.domain;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "Employees")
public class Employee {

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
    private String name;

    @OneToMany
    @JoinColumn(name = "contracts_id")
    private List<Contract> contracts;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Alocation> alocations;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Contract> getContracts() { return contracts; }

    public void setContracts(List<Contract> contracts) { this.contracts = contracts; }

    public List<Alocation> getAlocations() { return alocations; }

    public void setAlocations(List<Alocation> alocations) { this.alocations = alocations; }
}
