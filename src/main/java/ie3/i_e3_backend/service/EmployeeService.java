package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.EmployeeDTO;
import ie3.i_e3_backend.model.DTOs.EmployeeModalDTO;
import ie3.i_e3_backend.model.DTOs.ProjectInfoDTO;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.*;
import ie3.i_e3_backend.util.NotFoundException;
import ie3.i_e3_backend.util.OverAlocationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ParametersRepository parametersRepository;
    private final AlocationRepository alocationRepository;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    public EmployeeService(final EmployeeRepository employeeRepository, ContractRepository contractRepository, ParametersRepository parametersRepository , AlocationRepository alocationRepository , ProjectService projectService,
                           final ApplicationEventPublisher publisher, ProjectRepository projectRepository) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.parametersRepository = parametersRepository;
        this.alocationRepository = alocationRepository;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
    }

    public List<EmployeeDTO> findAll() {
        final List<Employee> employees = employeeRepository.findAll(Sort.by("id"));
        return employees.stream()
                .map(employee -> mapToDTO(employee, new EmployeeDTO()))
                .toList();
    }

    public EmployeeDTO get(final Long id) {
        return employeeRepository.findById(id)
                .map(employee -> mapToDTO(employee, new EmployeeDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public EmployeeModalDTO getEmployeeDetails(final Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        var currentContract = contractRepository.findTopActiveContractByEmployeeId(id, now)
                .stream().findFirst().orElse(null);

        return mapToModalDTO(employee, new EmployeeModalDTO(), currentContract);
    }

    @Transactional(readOnly = true)
    public List<EmployeeModalDTO> getEmployeesWithWeeklyHoursForProject(Long projectId, final int weeklyHours) {
        Project project = projectRepository.findById(projectId).orElseThrow(NotFoundException::new);

        Set<Long> allocatedEmployeeIds = project.getAlocations().stream()
                .map(alocation -> alocation.getEmployee().getId())
                .collect(Collectors.toSet());

        return employeeRepository.findAll(Sort.by("id")).stream()
                .filter(employee -> !allocatedEmployeeIds.contains(employee.getId()))
                .filter(employee -> isNotOverAllocated(
                        employee.getId(),
                        project.getStartDate().toLocalDate(),
                        project.getEndDate().toLocalDate(),
                        weeklyHours)
                )
                .map(employee -> {
                    Contract activeContract = contractRepository
                            .findTopActiveContractByEmployeeId(employee.getId(), OffsetDateTime.now())
                            .stream()
                            .findFirst()
                            .orElse(null);

                    return activeContract != null
                            ? mapToModalDTO(employee, new EmployeeModalDTO(), activeContract)
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public Long create(final EmployeeDTO employeeDTO) {
        final Employee employee = new Employee();
        mapToEntity(employeeDTO, employee);
        return employeeRepository.save(employee).getId();
    }

    private EmployeeDTO mapToDTO(final Employee employee, final EmployeeDTO employeeDTO) {
        employeeDTO.setId(employee.getId());
        employeeDTO.setName(employee.getName());
        return employeeDTO;
    }

    private Employee mapToEntity(final EmployeeDTO employeeDTO, final Employee employee) {
        employee.setName(employeeDTO.getName());
        return employee;
    }

   private EmployeeModalDTO mapToModalDTO(final Employee employee, final EmployeeModalDTO employeeModalDTO, final Contract contract) {
        employeeModalDTO.setId(employee.getId());
        employeeModalDTO.setName(employee.getName());
        employeeModalDTO.setActiveContract(contract != null);

        List<Role> employeeRoles = new ArrayList<>();
        if (contract != null) {
           contract.getProfile().forEach(profile -> {
                employeeRoles.add(profile.getRole());
           });
        }

       employeeModalDTO.setRoles(employeeRoles);

        List<Project> projects = employee.getAlocations().stream().map(Alocation::getProject).toList();
        List<ProjectInfoDTO> projectsDTO = projects.stream()
               .map(p -> new ProjectInfoDTO(p.getName(), projectService.getProjectStatus(p)))
               .toList();

        employeeModalDTO.setContractInfoList(projectsDTO);

        return employeeModalDTO;
   }

    private boolean isNotOverAllocated(final Long employeeId, final LocalDate startDate, final LocalDate endDate, int weekHours) {
        List<Alocation> overlappingAllocations = alocationRepository.findOverlappingAllocationsForEmployee(
                employeeId,
                startDate.atStartOfDay().atOffset(ZoneOffset.UTC),
                endDate.atStartOfDay().atOffset(ZoneOffset.UTC)
        );

        LocalDate weekIterator = startDate;
        while (!weekIterator.isAfter(endDate)) {
            double existingHoursThisWeek = 0;

            for (Alocation existingAllocation : overlappingAllocations) {
                existingHoursThisWeek += getProratedHoursForWeek(startDate, endDate, weekHours, weekIterator);
            }

            double newAllocationHoursThisWeek = getProratedHoursForWeek(startDate, endDate, weekHours, weekIterator);
            double expectedTotalHours = existingHoursThisWeek + newAllocationHoursThisWeek;

            double maxAllowed = parametersRepository.findMaxWeeklyHours(1L);
            if (expectedTotalHours > maxAllowed) {
                return false;
            }

            weekIterator = weekIterator.plusWeeks(1);
        }

        return true;
    }

    private double getProratedHoursForWeek(LocalDate startDate, LocalDate endDate, int weekHours ,LocalDate dateInWeek) {
        LocalDate projectStart = startDate;
        LocalDate projectEnd = endDate;

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

        double hoursPerDay = weekHours / 5.0;
        return hoursPerDay * workingDaysInWeek;
    }
}
