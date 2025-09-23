package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.AlocationDTO;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.AlocationRepository;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.repos.ParametersRepository;
import ie3.i_e3_backend.repos.ProjectRepository;
import ie3.i_e3_backend.util.NotFoundException;
import ie3.i_e3_backend.util.OverAlocationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
public class AlocationService {

    private final AlocationRepository alocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ParametersRepository parametersRepository;

    public AlocationService(final AlocationRepository alocationRepository,
                            final EmployeeRepository employeeRepository,
                            final ProjectRepository projectRepository,
                            final ParametersRepository parametersRepository) {
        this.alocationRepository = alocationRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.parametersRepository = parametersRepository;
    }

    public List<AlocationDTO> findAll() {
        final List<Alocation> alocations = alocationRepository.findAll(Sort.by("id"));
        return alocations.stream()
                .map(alocation -> mapToDTO(alocation, new AlocationDTO()))
                .toList();
    }

    public AlocationDTO get(final Long id) {
        return alocationRepository.findById(id)
                .map(alocation -> mapToDTO(alocation, new AlocationDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Long create(final AlocationDTO alocationDTO) {
        final Alocation alocation = new Alocation();
        mapToEntity(alocationDTO, alocation);

        validateNoOverAllocation(alocation);

        return alocationRepository.save(alocation).getId();
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
//            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
//                workingDaysInWeek++;
//            }
            workingDaysInWeek++;
            currentDate = currentDate.plusDays(1);
        }

//        5.0 uncomment above to only work days.
        double hoursPerDay = allocation.getWeeklyHours() / 7.0;
        return hoursPerDay * workingDaysInWeek;
    }

    private AlocationDTO mapToDTO(final Alocation alocation, final AlocationDTO alocationDTO) {
        alocationDTO.setId(alocation.getId());
        alocationDTO.setWeeklyHours(alocation.getWeeklyHours());
        alocationDTO.setEmployeeRole(alocation.getEmployeeRole());
        alocationDTO.setUser(alocation.getEmployee() == null ? null : alocation.getEmployee().getId());
        alocationDTO.setProject(alocation.getProject() == null ? null : alocation.getProject().getId());
        return alocationDTO;
    }

    private Alocation mapToEntity(final AlocationDTO alocationDTO, final Alocation alocation) {
        alocation.setWeeklyHours(alocationDTO.getWeeklyHours());
        alocation.setEmployeeRole(alocationDTO.getEmployeeRole());
        final Employee user = alocationDTO.getUser() == null ? null : employeeRepository.findById(alocationDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        alocation.setEmployee(user);
        final Project project = alocationDTO.getProject() == null ? null : projectRepository.findById(alocationDTO.getProject())
                .orElseThrow(() -> new NotFoundException("project not found"));
        alocation.setProject(project);
        return alocation;
    }

    public boolean managerExistsByProjectId(final Long managerId,  final Long projectId) {return alocationRepository.existsByProjectIdAndEmployeeRole(projectId, Role.MANAGER);}
}
