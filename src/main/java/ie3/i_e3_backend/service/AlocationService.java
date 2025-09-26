package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.AlocationDTO;
import ie3.i_e3_backend.model.DTOs.AlocationReadDTO;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.*;
import ie3.i_e3_backend.util.ExistentManagerException;
import ie3.i_e3_backend.util.InvalidProjectException;
import ie3.i_e3_backend.util.NotFoundException;
import ie3.i_e3_backend.util.OverAlocationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
public class AlocationService {

    private final AlocationRepository alocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ParametersRepository parametersRepository;
    private final ContractRepository contractRepository;

    public AlocationService(final AlocationRepository alocationRepository,
                            final EmployeeRepository employeeRepository,
                            final ProjectRepository projectRepository,
                            final ParametersRepository parametersRepository, ContractRepository contractRepository) {
        this.alocationRepository = alocationRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.parametersRepository = parametersRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public List<AlocationReadDTO> findAll() {
        final List<Alocation> alocations = alocationRepository.findAll(Sort.by("id"));
        return alocations.stream()
                .map(alocation -> mapToDTO(alocation, new AlocationReadDTO()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlocationReadDTO get(final Long id) {
        return alocationRepository.findById(id)
                .map(alocation -> mapToDTO(alocation, new AlocationReadDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Long create(final AlocationDTO alocationDTO) {
        final Alocation alocation = new Alocation();
        mapToEntity(alocationDTO, alocation);
        final Long projectId = alocation.getProject().getId();

        validateProjectDate(projectId);
        validateEmployeeRole(alocation);
        validateNoOverAllocation(alocation);
        if (alocation.getEmployeeRole().equals(Role.MANAGER)) validateProjectManager(projectId);



        return alocationRepository.save(alocation).getId();
    }

    private void validateEmployeeRole(final Alocation newAlocation) {
        Long employeeId = newAlocation.getEmployee().getId();
        OffsetDateTime now = OffsetDateTime.now();

        var currentContract = contractRepository.findTopActiveContractByEmployeeId(employeeId, now)
                .stream().findFirst().orElse(null);

        if (currentContract == null) {
            throw new NotFoundException("Active contract not found for employee");
        }

        boolean roleExists = currentContract.getProfile().stream()
                .anyMatch(profile -> profile.getRole().equals(newAlocation.getEmployeeRole()));

        if (!roleExists) {
            throw new NotFoundException("Employee role not found in active contract");
        }
    }

    private void validateProjectDate(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (project.getEndDate().isBefore(OffsetDateTime.now())) {
            throw new InvalidProjectException(projectId);
        }
    }

    private void validateProjectManager(Long projectId) {
        boolean hasManager = alocationRepository.findByProjectId(projectId).stream()
                .anyMatch(alocation -> alocation.getEmployeeRole().equals(Role.MANAGER));

        if (hasManager) {
            throw new ExistentManagerException(projectId);
        }
    }

    private void validateNoOverAllocation(final Alocation newAlocation) {
        List<Alocation> overlappingAllocations = alocationRepository.findOverlappingAllocationsForEmployee(
                newAlocation.getEmployee().getId(),
                newAlocation.getProject().getStartDate(),
                newAlocation.getProject().getEndDate()
        );

        LocalDate weekIterator = newAlocation.getProject().getStartDate().toLocalDate();
        while (!weekIterator.isAfter(newAlocation.getProject().getEndDate().toLocalDate())) {

            double existingHoursThisWeek = 0;

            for (Alocation existingAllocation : overlappingAllocations) {
                existingHoursThisWeek += getProratedHoursForWeek(existingAllocation, weekIterator);
            }

            double newAllocationHoursThisWeek = getProratedHoursForWeek(newAlocation, weekIterator);

            double expectedTotalHours = existingHoursThisWeek + newAllocationHoursThisWeek;
            if (expectedTotalHours > parametersRepository.findMaxWeeklyHours(1L)) {
                throw new OverAlocationException((int) expectedTotalHours, parametersRepository.findMaxWeeklyHours(1L));
            }

            weekIterator = weekIterator.plusWeeks(1);
        }
    }

    /*
        By default, it only counts the working days, excluding MONDAY and SUNDAY.
     */
    private double getProratedHoursForWeek(Alocation allocation, LocalDate dateInWeek) {
        LocalDate projectStart = allocation.getProject().getStartDate().toLocalDate();
        LocalDate projectEnd = allocation.getProject().getEndDate().toLocalDate();

        LocalDate weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate effectiveStart = projectStart.isAfter(weekStart) ? projectStart : weekStart;
        LocalDate effectiveEnd = projectEnd.isBefore(weekEnd) ? projectEnd : weekEnd;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0;
        }

        int workingDaysInWeek = 0;
        LocalDate currentDate = effectiveStart;
        while (!currentDate.isAfter(effectiveEnd)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDaysInWeek++;
            }
            currentDate = currentDate.plusDays(1);
        }

        double hoursPerDay = allocation.getWeeklyHours() / 5.0;
        return hoursPerDay * workingDaysInWeek;
    }

    private AlocationReadDTO mapToDTO(final Alocation alocation, final AlocationReadDTO alocationReadDTO) {
        alocationReadDTO.setId(alocation.getId());
        alocationReadDTO.setWeeklyHours(alocation.getWeeklyHours());
        alocationReadDTO.setEmployeeRole(alocation.getEmployeeRole());
        alocationReadDTO.setEmployee(alocation.getEmployee() == null ? null : alocation.getEmployee().getId());
        alocationReadDTO.setEmployeeName(alocation.getEmployee().getName());
        alocationReadDTO.setProject(alocation.getProject() == null ? null : alocation.getProject().getId());
        alocationReadDTO.setProjectName(alocation.getProject().getName());
        return alocationReadDTO;
    }

    private Alocation mapToEntity(final AlocationDTO alocationDTO, final Alocation alocation) {
        alocation.setWeeklyHours(alocationDTO.getWeeklyHours());
        alocation.setEmployeeRole(alocationDTO.getEmployeeRole());
        final Employee user = alocationDTO.getEmployee() == null ? null : employeeRepository.findById(alocationDTO.getEmployee())
                .orElseThrow(() -> new NotFoundException("user not found"));
        alocation.setEmployee(user);
        final Project project = alocationDTO.getProject() == null ? null : projectRepository.findById(alocationDTO.getProject())
                .orElseThrow(() -> new NotFoundException("project not found"));
        alocation.setProject(project);
        return alocation;
    }

    public boolean managerExistsByProjectId(final Long managerId,  final Long projectId) {return alocationRepository.existsByProjectIdAndEmployeeRole(projectId, Role.MANAGER);}
}
