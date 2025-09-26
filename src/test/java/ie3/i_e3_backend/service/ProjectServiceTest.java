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
import ie3.i_e3_backend.util.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Edge Cases Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AlocationRepository alocationRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private Employee testEmployee;
    private Contract testContract;
    private Alocation testAllocation;

    @BeforeEach
    void setUp() {
        // Set up test data
        testProject = createTestProject();
        testEmployee = createTestEmployee();
        testContract = createTestContract();
        testAllocation = createTestAllocation();
    }

    // Edge Case: Repository returns null or empty collections
    @Test
    @DisplayName("Should handle empty project list gracefully")
    void shouldHandleEmptyProjectList() {
        // Given
        when(projectRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        // When
        List<ProjectReadDTO> result = projectService.findAll();

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(projectRepository).findAll(Sort.by("id"));
    }

    @Test
    @DisplayName("Should return empty count status for empty project list")
    void shouldReturnEmptyCountStatusForEmptyList() {
        // Given
        when(projectRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        // When
        ProjectCountStatusDTO result = projectService.countByStatus();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalProjectCount()).isZero();
        assertThat(result.getCompletedProjectCount()).isZero();
        assertThat(result.getAvailableProjectCount()).isZero();
        assertThat(result.getPlannedProjectCount()).isZero();
        assertThat(result.getFinishedProjectCount()).isZero();
        assertThat(result.getUnavailableProjectCount()).isZero();
    }

    // Edge Case: Non-existent project ID
    @Test
    @DisplayName("Should throw NotFoundException when project does not exist")
    void shouldThrowNotFoundExceptionWhenProjectDoesNotExist() {
        // Given
        Long nonExistentId = 999L;
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.get(nonExistentId))
                .isInstanceOf(NotFoundException.class);

        verify(projectRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting cost for non-existent project")
    void shouldThrowNotFoundExceptionWhenGettingCostForNonExistentProject() {
        // Given
        Long nonExistentId = 999L;
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(30);
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getCost(nonExistentId, startDate, endDate))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting modal for non-existent project")
    void shouldThrowNotFoundExceptionWhenGettingModalForNonExistentProject() {
        // Given
        Long nonExistentId = 999L;
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(30);
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getModal(nonExistentId, startDate, endDate))
                .isInstanceOf(NotFoundException.class);
    }

    // Edge Case: Project with no allocations
    @Test
    @DisplayName("Should calculate zero cost for project with no allocations")
    void shouldCalculateZeroCostForProjectWithNoAllocations() {
        // Given
        testProject.setAlocations(Collections.emptyList());
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCost()).isZero();
        assertThat(result.getTotalCostPerPeriod()).isZero();
    }

    // Edge Case: Null date parameters
    @Test
    @DisplayName("Should use project dates when period dates are null")
    void shouldUseProjectDatesWhenPeriodDatesAreNull() {
        // Given
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCost()).isNotNegative();
        assertThat(result.getTotalCostPerPeriod()).isNotNegative();
    }

    // Edge Case: Invalid date ranges
    @Test
    @DisplayName("Should handle end date before start date")
    void shouldHandleEndDateBeforeStartDate() {
        // Given
        OffsetDateTime startDate = OffsetDateTime.now().plusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now().minusDays(30);
        when(projectRepository.findAllByDateRange(startDate, endDate)).thenReturn(Collections.emptyList());

        // When
        List<ProjectReadDTO> result = projectService.findAllByDateRange(startDate, endDate);

        // Then
        assertThat(result).isNotNull().isEmpty();
    }

    // Edge Case: Project status evaluation edge cases
    @Test
    @DisplayName("Should correctly evaluate project status with no roles and past end date")
    void shouldEvaluateProjectStatusFinishedWithNoRoles() {
        // Given
        Project pastProject = createPastProject();
        when(alocationRepository.findRolesByProjectId(pastProject.getId())).thenReturn(Collections.emptyList());

        // When
        ProjectStatus status = projectService.getProjectStatus(pastProject);

        // Then
        assertThat(status).isEqualTo(ProjectStatus.FINISHED);
    }

    @Test
    @DisplayName("Should correctly evaluate project status with no roles and active dates")
    void shouldEvaluateProjectStatusUnavailableWithNoRoles() {
        // Given
        Project activeProject = createActiveProject();
        when(alocationRepository.findRolesByProjectId(activeProject.getId())).thenReturn(Collections.emptyList());

        // When
        ProjectStatus status = projectService.getProjectStatus(activeProject);

        // Then
        assertThat(status).isEqualTo(ProjectStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("Should correctly evaluate project status with all required roles and active dates")
    void shouldEvaluateProjectStatusAvailableWithAllRoles() {
        // Given
        Project activeProject = createActiveProject();
        when(alocationRepository.findRolesByProjectId(activeProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));

        // When
        ProjectStatus status = projectService.getProjectStatus(activeProject);

        // Then
        assertThat(status).isEqualTo(ProjectStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should correctly evaluate project status with partial roles")
    void shouldEvaluateProjectStatusWithPartialRoles() {
        // Given
        Project activeProject = createActiveProject();
        when(alocationRepository.findRolesByProjectId(activeProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.QA)); // Missing MANAGER

        // When
        ProjectStatus status = projectService.getProjectStatus(activeProject);

        // Then
        assertThat(status).isEqualTo(ProjectStatus.UNAVAILABLE);
    }

    // Edge Case: Weekend calculation in cost calculation
    @Test
    @DisplayName("Should exclude weekends from cost calculation")
    void shouldExcludeWeekendsFromCostCalculation() {
        // Given - Create a project that spans exactly one week (Mon-Sun)
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        OffsetDateTime startDate = monday.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDate = monday.plusDays(6).atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Project weekProject = createProjectWithDates(startDate, endDate);
        Alocation allocation = createTestAllocation();
        allocation.setWeeklyHours(40); // 8 hours per day

        allocation.setProject(weekProject);
        weekProject.setAlocations(List.of(allocation));

        Contract contract = createContractForPeriod(startDate, endDate);
        contract.setWageByHour(10.0);

        when(projectRepository.findById(weekProject.getId())).thenReturn(Optional.of(weekProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(contract));

        // When
        ProjectCostDTO result = projectService.getCost(weekProject.getId(), null, null);

        // Then - Should only count 5 working days (Mon-Fri), not 7
        double expectedDailyCost = (40.0 / 5.0) * 10.0; // (weekly hours / 5 days) * wage
        double expectedTotalCost = expectedDailyCost * 5; // 5 working days

        assertThat(result.getTotalCostPerPeriod()).isEqualTo(expectedTotalCost);
    }

    // Edge Case: Contract gaps
    @Test
    @DisplayName("Should handle days without valid contracts")
    void shouldHandleDaysWithoutValidContracts() {
        // Given
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList()); // No contracts

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then
        assertThat(result.getTotalCost()).isZero();
        assertThat(result.getTotalCostPerPeriod()).isZero();
    }

    // Edge Case: Large numbers and potential overflow
    @Test
    @DisplayName("Should handle large cost calculations without overflow")
    void shouldHandleLargeCostCalculationsWithoutOverflow() {
        // Given
        Alocation highHoursAllocation = createTestAllocation();
        highHoursAllocation.setWeeklyHours(Integer.MAX_VALUE / 1000); // Very high hours
        testProject.setAlocations(List.of(highHoursAllocation));

        Contract highWageContract = createTestContract();
        highWageContract.setWageByHour(1000.0); // High wage

        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(highWageContract));

        // When & Then - Should not throw exception
        assertThatCode(() -> projectService.getCost(testProject.getId(), null, null))
                .doesNotThrowAnyException();
    }

    // Edge Case: Filter edge cases for less finished and complete
    @Test
    @DisplayName("Should filter out finished and completed projects correctly")
    void shouldFilterOutFinishedAndCompletedProjects() {
        // Given
        Project finishedProject = createPastProject();
        Project completedProject = createPastProject();
        Project activeProject = createActiveProject();

        List<Project> allProjects = List.of(finishedProject, completedProject, activeProject);
        when(projectRepository.findAll(any(Sort.class))).thenReturn(allProjects);

        // Mock role checks to make one finished and one completed
        when(alocationRepository.findRolesByProjectId(finishedProject.getId()))
                .thenReturn(Collections.emptyList()); // No roles = FINISHED
        when(alocationRepository.findRolesByProjectId(completedProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER)); // All roles = COMPLETED
        when(alocationRepository.findRolesByProjectId(activeProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER)); // All roles = AVAILABLE (active)

        // When
        List<ProjectReadDTO> result = projectService.findAllLessFinishedAndComplete();

        // Then
        assertThat(result).hasSize(1); // Only the active project should remain
    }

    // Edge Case: Boundary date calculations
    @Test
    @DisplayName("Should handle project period boundary dates correctly")
    void shouldHandleProjectPeriodBoundaryDatesCorrectly() {
        // Given - Period extends beyond project boundaries
        OffsetDateTime projectStart = OffsetDateTime.now();
        OffsetDateTime projectEnd = OffsetDateTime.now().plusDays(10);

        OffsetDateTime periodStart = projectStart.minusDays(5); // Before project start
        OffsetDateTime periodEnd = projectEnd.plusDays(5); // After project end

        Project boundaryProject = createProjectWithDates(projectStart, projectEnd);

        Alocation boundaryAllocation = createTestAllocation();
        boundaryAllocation.setProject(boundaryProject);
        boundaryProject.setAlocations(List.of(boundaryAllocation));

        when(projectRepository.findById(boundaryProject.getId())).thenReturn(Optional.of(boundaryProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(boundaryProject.getId(), periodStart, periodEnd);

        // Then - Should calculate cost only for the project period
        assertThat(result.getTotalCostPerPeriod()).isGreaterThan(0);
    }

    // Helper methods for creating test data
    private Project createTestProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setStartDate(OffsetDateTime.now().minusDays(10));
        project.setEndDate(OffsetDateTime.now().plusDays(10));
        return project;
    }

    private Project createPastProject() {
        Project project = new Project();
        project.setId(2L);
        project.setName("Past Project");
        project.setDescription("Past Description");
        project.setStartDate(OffsetDateTime.now().minusDays(30));
        project.setEndDate(OffsetDateTime.now().minusDays(10));
        return project;
    }

    private Project createActiveProject() {
        Project project = new Project();
        project.setId(3L);
        project.setName("Active Project");
        project.setDescription("Active Description");
        project.setStartDate(OffsetDateTime.now().minusDays(5));
        project.setEndDate(OffsetDateTime.now().plusDays(15));
        return project;
    }

    private Project createProjectWithDates(OffsetDateTime startDate, OffsetDateTime endDate) {
        Project project = new Project();
        project.setId(4L);
        project.setName("Custom Date Project");
        project.setDescription("Custom dates");
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return project;
    }

    private Employee createTestEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Test Employee");
        return employee;
    }

    private Contract createTestContract() {
        Contract contract = new Contract();
        contract.setId(1L);
        contract.setEmployee(testEmployee);
        contract.setWageByHour(20.0);
        contract.setStartDate(OffsetDateTime.now().minusDays(30));
        contract.setEndDate(OffsetDateTime.now().plusDays(30));
        return contract;
    }

    private Contract createContractForPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        Contract contract = new Contract();
        contract.setId(2L);
        contract.setEmployee(testEmployee);
        contract.setWageByHour(10.0);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        return contract;
    }

    private Alocation createTestAllocation() {
        Alocation allocation = new Alocation();
        allocation.setId(1L);
        allocation.setProject(testProject);
        allocation.setEmployee(testEmployee);
        allocation.setEmployeeRole(Role.DEV);
        allocation.setWeeklyHours(40);
        return allocation;
    }
}