package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.*;
import ie3.i_e3_backend.model.Enums.ProjectStatus;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.AlocationRepository;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Integration and Realistic Scenarios Tests")
class ProjectServiceIntegrationTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AlocationRepository alocationRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("Should handle realistic multi-employee project cost calculation")
    void shouldHandleRealisticMultiEmployeeProjectCostCalculation() {
        // Given - A project with multiple employees in different roles
        Project project = createRealisticProject();

        // ... (Employee and Contract setup is the same as before) ...
        Employee seniorDev = createEmployee(1L, "Lucas Senior");
        Contract seniorContract = createContract(1L, seniorDev, 50.0,
                project.getStartDate(), project.getEndDate());
        Alocation seniorAllocation = createAllocation(1L, project, seniorDev, Role.DEV, 40);

        Employee juniorDev = createEmployee(2L, "Matheus Junior");
        Contract juniorContract = createContract(2L, juniorDev, 25.0,
                project.getStartDate(), project.getEndDate());
        Alocation juniorAllocation = createAllocation(2L, project, juniorDev, Role.DEV, 40);

        Employee qaEngineer = createEmployee(3L, "Fernando QA");
        Contract qaContract = createContract(3L, qaEngineer, 35.0,
                project.getStartDate(), project.getEndDate());
        Alocation qaAllocation = createAllocation(3L, project, qaEngineer, Role.QA, 30);

        Employee projectManager = createEmployee(4L, "Alberto Manager");
        Contract managerContract = createContract(4L, projectManager, 60.0,
                project.getStartDate(), project.getEndDate());
        Alocation managerAllocation = createAllocation(4L, project, projectManager, Role.MANAGER, 20);

        project.setAlocations(List.of(seniorAllocation, juniorAllocation, qaAllocation, managerAllocation));

        // Mock repository calls
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(contractRepository.findAllByEmployeeIdAndDateRange(eq(seniorDev.getId()), any(), any()))
                .thenReturn(List.of(seniorContract));
        when(contractRepository.findAllByEmployeeIdAndDateRange(eq(juniorDev.getId()), any(), any()))
                .thenReturn(List.of(juniorContract));
        when(contractRepository.findAllByEmployeeIdAndDateRange(eq(qaEngineer.getId()), any(), any()))
                .thenReturn(List.of(qaContract));
        when(contractRepository.findAllByEmployeeIdAndDateRange(eq(projectManager.getId()), any(), any()))
                .thenReturn(List.of(managerContract));

        // When
        ProjectCostDTO result = projectService.getCost(project.getId(), null, null);

        // Then - Calculate expected cost based on the project's ACTUAL number of working days
        long actualWorkingDays = countWorkingDays(
                project.getStartDate().toLocalDate(),
                project.getEndDate().toLocalDate()
        );

        double expectedSeniorCost = (40.0 / 5.0) * 50.0 * actualWorkingDays;
        double expectedJuniorCost = (40.0 / 5.0) * 25.0 * actualWorkingDays;
        double expectedQaCost = (30.0 / 5.0) * 35.0 * actualWorkingDays;
        double expectedManagerCost = (20.0 / 5.0) * 60.0 * actualWorkingDays;
        double expectedTotalCost = expectedSeniorCost + expectedJuniorCost + expectedQaCost + expectedManagerCost;

        assertThat(result.getTotalCost()).isEqualTo(expectedTotalCost);
        assertThat(result.getTotalCostPerPeriod()).isEqualTo(expectedTotalCost);
    }

    @Test
    @DisplayName("Should handle project with employee contract changes mid-project")
    void shouldHandleProjectWithEmployeeContractChangesMidProject() {
        // Given - A project where an employee gets a raise mid-project
        Project project = createRealisticProject();
        Employee developer = createEmployee(1L, "Lucas Developer");

        // Initial contract with lower wage
        OffsetDateTime contractChangeDate = project.getStartDate().plusDays(5);
        Contract initialContract = createContract(1L, developer, 30.0,
                project.getStartDate(), contractChangeDate.minusDays(1));

        // New contract with higher wage
        Contract newContract = createContract(2L, developer, 40.0,
                contractChangeDate, project.getEndDate());

        Alocation allocation = createAllocation(1L, project, developer, Role.DEV, 40);
        project.setAlocations(List.of(allocation));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(contractRepository.findAllByEmployeeIdAndDateRange(eq(developer.getId()), any(), any()))
                .thenReturn(List.of(initialContract, newContract));

        // When
        ProjectCostDTO result = projectService.getCost(project.getId(), null, null);

        // Then
        double dailyHours = 40.0 / 5.0;

        long firstPeriodWorkingDays = countWorkingDays(
                initialContract.getStartDate().toLocalDate(),
                initialContract.getEndDate().toLocalDate()
        );

        long secondPeriodWorkingDays = countWorkingDays(
                newContract.getStartDate().toLocalDate(),
                newContract.getEndDate().toLocalDate()
        );

        double firstPeriodCost = dailyHours * 30.0 * firstPeriodWorkingDays;
        double secondPeriodCost = dailyHours * 40.0 * secondPeriodWorkingDays;
        double expectedTotalCost = firstPeriodCost + secondPeriodCost;

        assertThat(result.getTotalCost()).isEqualTo(expectedTotalCost);
    }

    @Test
    @DisplayName("Should correctly evaluate complex project status scenarios")
    void shouldCorrectlyEvaluateComplexProjectStatusScenarios() {
        // Given - Multiple projects with different characteristics
        List<Project> projects = createVariousProjects();
        
        // Mock role checks for different scenarios
        when(alocationRepository.findRolesByProjectId(1L)) // Future project with all roles
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(2L)) // Active project with all roles
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(3L)) // Past project with all roles
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(4L)) // Active project missing QA
                .thenReturn(List.of(Role.DEV, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(5L)) // Past project with no roles
                .thenReturn(Collections.emptyList());

        when(projectRepository.findAll(any(Sort.class))).thenReturn(projects);

        // When
        ProjectCountStatusDTO result = projectService.countByStatus();

        // Then
        assertThat(result.getTotalProjectCount()).isEqualTo(5);
        assertThat(result.getPlannedProjectCount()).isEqualTo(1);     // Future with all roles
        assertThat(result.getAvailableProjectCount()).isEqualTo(1);   // Active with all roles
        assertThat(result.getCompletedProjectCount()).isEqualTo(1);   // Past with all roles
        assertThat(result.getUnavailableProjectCount()).isEqualTo(1); // Active without all roles
        assertThat(result.getFinishedProjectCount()).isEqualTo(1);    // Past with no roles
    }

    @Test
    @DisplayName("Should handle project modal with mixed employee roles")
    void shouldHandleProjectModalWithMixedEmployeeRoles() {
        // Given - A project with employees in multiple roles
        Project project = createRealisticProject();
        
        Employee fullStackDev = createEmployee(1L, "Alberto FullStack");
        Employee leadDev = createEmployee(2L, "Lucas Lead");
        Employee qaEngineer = createEmployee(3L, "Fernando QA");

        // Create allocations
        Alocation fullStackAllocation = createAllocation(1L, project, fullStackDev, Role.DEV, 40);
        Alocation leadAllocation = createAllocation(2L, project, leadDev, Role.MANAGER, 30);
        Alocation qaAllocation = createAllocation(3L, project, qaEngineer, Role.QA, 35);

        project.setAlocations(List.of(fullStackAllocation, leadAllocation, qaAllocation));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(alocationRepository.findRolesByProjectId(project.getId()))
                .thenReturn(List.of(Role.DEV, Role.MANAGER, Role.QA));

        // Mock contracts for cost calculation
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(createContract(1L, fullStackDev, 45.0, 
                        project.getStartDate(), project.getEndDate())));

        // When
        ProjectModalDTO result = projectService.getModal(project.getId(), null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(project.getName());
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.AVAILABLE);
        assertThat(result.getEmployees()).hasSize(3);
        assertThat(result.getEmployees()).containsEntry("Alberto FullStack", Role.DEV);
        assertThat(result.getEmployees()).containsEntry("Lucas Lead", Role.MANAGER);
        assertThat(result.getEmployees()).containsEntry("Fernando QA", Role.QA);
        assertThat(result.getCosts()).isNotNull();
    }

    @Test
    @DisplayName("Should handle filtering less finished and complete with realistic data")
    void shouldHandleFilteringLessFinishedAndCompleteWithRealisticData() {
        // Given - Mix of projects in different states
        List<Project> allProjects = createVariousProjects();

        // Mock repository and role checks
        when(projectRepository.findAll(any(Sort.class))).thenReturn(allProjects);
        
        // Project states based on our test data:
        // 1L: Future project with all roles -> PLANNED (should be included)
        // 2L: Active project with all roles -> AVAILABLE (should be included)
        // 3L: Past project with all roles -> COMPLETED (should be filtered out)
        // 4L: Active project missing roles -> UNAVAILABLE (should be included)
        // 5L: Past project with no roles -> FINISHED (should be filtered out)
        
        when(alocationRepository.findRolesByProjectId(1L))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(2L))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(3L))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));
        when(alocationRepository.findRolesByProjectId(4L))
                .thenReturn(List.of(Role.DEV, Role.MANAGER)); // Missing QA
        when(alocationRepository.findRolesByProjectId(5L))
                .thenReturn(Collections.emptyList()); // No roles

        // When
        List<ProjectReadDTO> result = projectService.findAllLessFinishedAndComplete();

        // Then - Should return only PLANNED, AVAILABLE, and UNAVAILABLE projects
        assertThat(result).hasSize(3);
        
        // Verify the correct projects are included
        Set<String> projectNames = result.stream()
                .map(ProjectReadDTO::getName)
                .collect(java.util.stream.Collectors.toSet());
        
        assertThat(projectNames).contains("Future Project", "Active Project", "Understaffed Project");
        assertThat(projectNames).doesNotContain("Past Project", "Legacy Project");
    }

    @Test
    @DisplayName("Should handle date range queries with realistic boundaries")
    void shouldHandleDateRangeQueriesWithRealisticBoundaries() {
        // Given - Projects spanning different time periods
        OffsetDateTime searchStart = OffsetDateTime.now().minusDays(15);
        OffsetDateTime searchEnd = OffsetDateTime.now().plusDays(15);

        List<Project> projectsInRange = List.of(
                createProjectWithDates(2L, "Overlapping Start", 
                        searchStart.minusDays(5), searchStart.plusDays(5)),
                createProjectWithDates(3L, "Fully Within", 
                        searchStart.plusDays(2), searchEnd.minusDays(2)),
                createProjectWithDates(4L, "Overlapping End", 
                        searchEnd.minusDays(5), searchEnd.plusDays(5))
        );

        when(projectRepository.findAllByDateRange(searchStart, searchEnd))
                .thenReturn(projectsInRange);

        // When
        List<ProjectReadDTO> result = projectService.findAllByDateRange(searchStart, searchEnd);

        // Then
        assertThat(result).hasSize(3);
        Set<String> names = result.stream()
                .map(ProjectReadDTO::getName)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(names).containsExactlyInAnyOrder(
                "Overlapping Start", "Fully Within", "Overlapping End");
    }

    // Helper methods for creating realistic test data
    private Project createRealisticProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Web Application Development");
        project.setDescription("Development of customer portal web application");
        project.setStartDate(OffsetDateTime.now().minusDays(5));
        project.setEndDate(OffsetDateTime.now().plusDays(5)); // 2 weeks total, 10 working days
        return project;
    }

    private long countWorkingDays(LocalDate start, LocalDate end) {
        long workingDays = 0;
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            DayOfWeek day = cursor.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            cursor = cursor.plusDays(1);
        }
        return workingDays;
    }

    private Employee createEmployee(Long id, String name) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName(name);
        return employee;
    }

    private Contract createContract(Long id, Employee employee, Double wageByHour,
                                   OffsetDateTime startDate, OffsetDateTime endDate) {
        Contract contract = new Contract();
        contract.setId(id);
        contract.setEmployee(employee);
        contract.setWageByHour(wageByHour);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        return contract;
    }

    private Alocation createAllocation(Long id, Project project, Employee employee,
                                      Role role, Integer weeklyHours) {
        Alocation allocation = new Alocation();
        allocation.setId(id);
        allocation.setProject(project);
        allocation.setEmployee(employee);
        allocation.setEmployeeRole(role);
        allocation.setWeeklyHours(weeklyHours);
        return allocation;
    }

    private List<Project> createVariousProjects() {
        OffsetDateTime now = OffsetDateTime.now();
        
        return List.of(
                createProjectWithDates(1L, "Future Project", now.plusDays(10), now.plusDays(20)),
                createProjectWithDates(2L, "Active Project", now.minusDays(5), now.plusDays(10)),
                createProjectWithDates(3L, "Past Project", now.minusDays(20), now.minusDays(5)),
                createProjectWithDates(4L, "Understaffed Project", now.minusDays(2), now.plusDays(15)),
                createProjectWithDates(5L, "Legacy Project", now.minusDays(30), now.minusDays(15))
        );
    }

    private Project createProjectWithDates(Long id, String name, 
                                          OffsetDateTime startDate, OffsetDateTime endDate) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription("Test project: " + name);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return project;
    }
}