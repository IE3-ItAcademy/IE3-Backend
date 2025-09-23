package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.ProjectCostDTO;
import ie3.i_e3_backend.model.DTOs.ProjectDTO;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.ProjectRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;

    public ProjectService(final ProjectRepository projectRepository, final ContractRepository contractRepository) {
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
    }

    public List<ProjectDTO> findAll() {
        final List<Project> projects = projectRepository.findAll(Sort.by("id"));
        return projects.stream()
                .map(project -> mapToDTO(project, new ProjectDTO()))
                .toList();
    }

    public ProjectDTO get(final Long id) {
        return projectRepository.findById(id)
                .map(project -> mapToDTO(project, new ProjectDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final ProjectDTO projectDTO) {
        final Project project = new Project();
        mapToEntity(projectDTO, project);
        return projectRepository.save(project).getId();
    }

    @Transactional(readOnly = true)
    public ProjectCostDTO getCost(final Long id) {
        ProjectCostDTO projectCostDTO = new ProjectCostDTO();
        projectCostDTO.setTotalCost(totalProjectCost(id));

        return projectCostDTO;
    }

    /*
        Total Project Cost =
        For each Allocation:
            - Determine the employee's active contract for each week of the project.
            - Calculate the number of business days (Mon–Fri) in that week where both:
                • the project is active
                • the contract is valid
            - Prorate the weekly hours based on those working days.
            - Multiply the prorated hours by the wage per hour from the contract.
        Sum all weekly costs across all allocations to get the final project cost.
    */
    private double totalProjectCost(final Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(NotFoundException::new);

        double totalCost = 0;

        for (Alocation alocation : project.getAlocations()) {
            LocalDate currentWeek = project.getStartDate().toLocalDate();

            while (!currentWeek.isAfter(project.getEndDate().toLocalDate())) {
                OffsetDateTime weekReferenceDate = currentWeek.atStartOfDay().atOffset(ZoneOffset.UTC);

                Optional<Contract> contractOpt = contractRepository.findTopActiveContractByEmployeeId(
                        alocation.getEmployee().getId(),
                        weekReferenceDate
                );

                if (contractOpt.isEmpty()) {
                    currentWeek = currentWeek.plusWeeks(1);
                    continue; // No active contract for this week
                }

                Contract contract = contractOpt.get();
                double wageByHour = contract.getWageByHour();

                double proratedHours = getProratedHoursForWeek(
                        project.getStartDate().toLocalDate(),
                        project.getEndDate().toLocalDate(),
                        currentWeek,
                        alocation.getWeeklyHours(),
                        contract.getStartDate().toLocalDate(),
                        contract.getEndDate().toLocalDate()
                );

                double weeklyCost = wageByHour * proratedHours;
                totalCost += weeklyCost;

                currentWeek = currentWeek.plusWeeks(1);
            }
        }

        return totalCost;
    }

    private double getProratedHoursForWeek(
            LocalDate projectStart,
            LocalDate projectEnd,
            LocalDate dateInWeek,
            int weeklyHours,
            LocalDate contractStart,
            LocalDate contractEnd
    ) {
        LocalDate weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = dateInWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate effectiveStart = Stream.of(projectStart, contractStart, weekStart)
                .max(LocalDate::compareTo).get();
        LocalDate effectiveEnd = Stream.of(projectEnd, contractEnd, weekEnd)
                .min(LocalDate::compareTo).get();

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0;
        }

        int workingDays = 0;
        for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }

        double hoursPerDay = weeklyHours / 5.0;
        return hoursPerDay * workingDays;
    }

    private ProjectDTO mapToDTO(final Project project, final ProjectDTO projectDTO) {
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setStartDate(project.getStartDate());
        projectDTO.setEndDate(project.getEndDate());
        projectDTO.setDescription(project.getDescription());
        return projectDTO;
    }

    private Project mapToEntity(final ProjectDTO projectDTO, final Project project) {
        project.setName(projectDTO.getName());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setDescription(projectDTO.getDescription());
        return project;
    }

}
