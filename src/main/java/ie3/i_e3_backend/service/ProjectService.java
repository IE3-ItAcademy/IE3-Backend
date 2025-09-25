package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.*;
import ie3.i_e3_backend.model.Enums.ProjectStatus;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.AlocationRepository;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.ProjectRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final AlocationRepository alocationRepository;

    public ProjectService(final ProjectRepository projectRepository, final ContractRepository contractRepository, final AlocationRepository alocationRepository) {
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
        this.alocationRepository = alocationRepository;
    }

    public List<ProjectReadDTO> findAll() {
        final List<Project> projects = projectRepository.findAll(Sort.by("id"));
        return projects.stream()
                .map(project -> mapToDTO(project, new ProjectReadDTO()))
                .toList();
    }

    public ProjectCountStatusDTO countByStatus() {
        final List<Project> projects = projectRepository.findAll(Sort.by("id"));
        return getProjectCountStatus(projects);
    }

    public ProjectReadDTO get(final Long id) {
        return projectRepository.findById(id)
                .map(project -> mapToDTO(project, new ProjectReadDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public List<ProjectReadDTO> findAllByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return projectRepository.findAllByDateRange(startDate, endDate).stream()
                .map(project -> mapToDTO(project, new ProjectReadDTO()))
                .toList();
    }

    public Long create(final ProjectDTO projectDTO) {
        final Project project = new Project();
        mapToEntity(projectDTO, project);
        return projectRepository.save(project).getId();
    }

    @Transactional(readOnly = true)
    public ProjectModalDTO getModal(final Long id, OffsetDateTime startDate, OffsetDateTime endDate) {
        return projectRepository.findById(id)
                .map(project -> mapToModalDTO(project, new ProjectModalDTO(), startDate, endDate))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public ProjectCostDTO getCost(final Long id, OffsetDateTime startDate, OffsetDateTime endDate) {
        Project project = projectRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        ProjectCostDTO projectCostDTO = new ProjectCostDTO();
        projectCostDTO.setTotalCost(totalProjectCost(id));

        if (startDate != null && endDate != null) {
            projectCostDTO.setTotalCostPerPeriod(periodTotalCost(id, startDate, endDate));
        } else {
            projectCostDTO.setTotalCostPerPeriod(periodTotalCost(id, project.getStartDate(), project.getEndDate()));
        }

        return projectCostDTO;
    }

    private double totalProjectCost(final Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(NotFoundException::new);

        return project.getAlocations().stream()
                .mapToDouble(alocation -> calculateAlocationCostPerPeriod(alocation, project.getStartDate(), project.getEndDate()))
                .sum();
    }

    private double periodTotalCost(final Long projectId, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(NotFoundException::new);

        return project.getAlocations().stream()
                .mapToDouble(alocation -> calculateAlocationCostPerPeriod(alocation, periodStart, periodEnd))
                .sum();
    }

    /*
        Project Cost =
        For each Allocation:
            - Determine the employee's active contract for each week of the project.
            - Calculate the number of business days (Mon–Fri) in that day where both:
                • the project is active
                • the contract is valid
            - Prorate the weekly hours based on those working days.
            - Multiply the prorated hours by the wage per hour from the contract.
        Sum all weekly costs across all allocations to get the final project cost.

        Summarizing:
        Total Cost = Number of business days × Daily working hours × Wage per hour
    */
    private double calculateAlocationCostPerPeriod(
            Alocation alocation,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    ) {
        double totalCost = 0;

        Project project = projectRepository.findById(alocation.getProject().getId())
                .orElseThrow(NotFoundException::new);

        LocalDate projectStartDate = project.getStartDate().toLocalDate();
        LocalDate projectEndDate = project.getEndDate().toLocalDate();

        LocalDate currentDate = periodStart.toLocalDate();
        LocalDate endDate = periodEnd.toLocalDate();

        if (currentDate.isBefore(projectStartDate)) {
            currentDate = projectStartDate;
        }

        if (endDate.isAfter(projectEndDate)) {
            endDate = projectEndDate;
        }

        // 5 Business Day
        double dailyHours = alocation.getWeeklyHours() / 5.0;

        List<Contract> contracts = contractRepository.findAllByEmployeeIdAndDateRange(
                alocation.getEmployee().getId(),
                periodStart,
                periodEnd
        );

        Map<LocalDate, Contract> contractCache = new HashMap<>();
        for (Contract contract : contracts) {
            LocalDate start = contract.getStartDate().toLocalDate();
            LocalDate end = contract.getEndDate().toLocalDate();

            if (start.isBefore(periodStart.toLocalDate())) {
                start = periodStart.toLocalDate();
            }
            if (end.isAfter(periodEnd.toLocalDate())) {
                end = periodEnd.toLocalDate();
            }

            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                contractCache.put(cursor, contract);
                cursor = cursor.plusDays(1);
            }
        }

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek day = currentDate.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                Contract contract = contractCache.get(currentDate);
                if (contract != null) {
                    totalCost += contract.getWageByHour() * dailyHours;
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        return totalCost;
    }

    public ProjectStatus getProjectStatus(Project project) {
        boolean hasRoles = checkProjectRoles(project.getId());
        
        return ProjectStatus.evaluate(project, hasRoles);
    }

    private boolean checkProjectRoles(Long projectId) {
        List<Role> projectRoles = alocationRepository.findRolesByProjectId(projectId);
        Set<Role> roleSet = new HashSet<>(projectRoles);

        return roleSet.containsAll(List.of(Role.DEV, Role.QA, Role.MANAGER));
    }

    private ProjectCountStatusDTO getProjectCountStatus(List<Project> projects) {
        ProjectCountStatusDTO projectCountStatusDTO = new ProjectCountStatusDTO();

        int completed = 0;
        int available = 0;
        int planned = 0;
        int finished = 0;
        int unavailable = 0;

        for (Project project : projects) {
            ProjectStatus status = getProjectStatus(project);

            switch (status) {
                case COMPLETED:
                    completed++;
                    break;
                case AVAILABLE:
                    available++;
                    break;
                case PLANNED:
                    planned++;
                    break;
                case FINISHED:
                    finished++;
                    break;
                case UNAVAILABLE:
                    unavailable++;
                    break;
            }
        }

        projectCountStatusDTO.setTotalProjectCount(projects.size());
        projectCountStatusDTO.setCompletedProjectCount(completed);
        projectCountStatusDTO.setAvailableProjectCount(available);
        projectCountStatusDTO.setPlannedProjectCount(planned);
        projectCountStatusDTO.setFinishedProjectCount(finished);
        projectCountStatusDTO.setUnavailableProjectCount(unavailable);

        return projectCountStatusDTO;
    }

    private ProjectReadDTO mapToDTO(final Project project, final ProjectReadDTO projectReadDTO) {
        projectReadDTO.setId(project.getId());
        projectReadDTO.setName(project.getName());
        projectReadDTO.setStartDate(project.getStartDate());
        projectReadDTO.setEndDate(project.getEndDate());
        projectReadDTO.setDescription(project.getDescription());
        projectReadDTO.setStatus(getProjectStatus(project));
        return projectReadDTO;
    }

    private ProjectModalDTO mapToModalDTO(final Project project, final ProjectModalDTO projectModalDTO, OffsetDateTime startDate, OffsetDateTime endDate) {
        projectModalDTO.setName(project.getName());
        projectModalDTO.setStatus(getProjectStatus(project));
        projectModalDTO.setDescription(project.getDescription());
        projectModalDTO.setStartDate(project.getStartDate());
        projectModalDTO.setEndDate(project.getEndDate());

        ProjectCostDTO projectCostDTO = new ProjectCostDTO();
        projectCostDTO.setTotalCost(totalProjectCost(project.getId()));

        if (startDate != null && endDate != null) {
            projectCostDTO.setTotalCostPerPeriod(periodTotalCost(project.getId(), startDate, endDate));
        } else {
            projectCostDTO.setTotalCostPerPeriod(periodTotalCost(project.getId(), project.getStartDate(), project.getEndDate()));
        }

        Set<String> uniqueEmployeeNames = project.getAlocations().stream()
                .map(alocation -> alocation.getEmployee().getName())
                .collect(Collectors.toSet());

        projectModalDTO.setCosts(projectCostDTO);

        projectModalDTO.setEmployees(uniqueEmployeeNames);

        return projectModalDTO;
    }

    private Project mapToEntity(final ProjectDTO projectDTO, final Project project) {
        project.setName(projectDTO.getName());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setDescription(projectDTO.getDescription());
        return project;
    }

}
