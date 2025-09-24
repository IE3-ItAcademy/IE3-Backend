package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.ProjectCostDTO;
import ie3.i_e3_backend.model.DTOs.ProjectDTO;
import ie3.i_e3_backend.model.DTOs.ProjectModalDTO;
import ie3.i_e3_backend.model.DTOs.ProjectReadDTO;
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

    public ProjectReadDTO get(final Long id) {
        return projectRepository.findById(id)
                .map(project -> mapToDTO(project, new ProjectReadDTO()))
                .orElseThrow(NotFoundException::new);
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
        LocalDate currentDate = periodStart.toLocalDate();
        LocalDate endDate = periodEnd.toLocalDate();

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

    private ProjectStatus getProjectStatus(Project project) {
        OffsetDateTime now = OffsetDateTime.now();

        if (project.getEndDate().isBefore(now)) {
            return ProjectStatus.COMPLETED;
        } else if (project.getStartDate().isAfter(now)) {
            return ProjectStatus.PLANNED;
        } else if (checkProjectRoles(project.getId())) {
            return ProjectStatus.AVAILABLE;
        }

        return ProjectStatus.UNAVAILABLE;
    }

    private boolean checkProjectRoles(Long projectId) {
        List<Role> projectRoles = alocationRepository.findRolesByProjectId(projectId);
        Set<Role> roleSet = new HashSet<>(projectRoles);

        return roleSet.containsAll(List.of(Role.DEV, Role.SECURITY, Role.QA, Role.MANAGER));
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
