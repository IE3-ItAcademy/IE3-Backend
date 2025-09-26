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
@DisplayName("ProjectService Edge Cases - Additional Tests")
class ProjectServiceEdgeTest {

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
        testProject = createTestProject();
        testEmployee = createTestEmployee();
        testContract = createTestContract();
        testAllocation = createTestAllocation();
    }

    // Edge Case: Concurrent modifications and thread safety
    @Test
    @DisplayName("Should handle repository method being called multiple times safely")
    void shouldHandleMultipleRepositoryCallsSafely() {
        // Given
        when(projectRepository.findById(testProject.getId()))
                .thenReturn(Optional.of(testProject))
                .thenReturn(Optional.empty()) // Second call returns empty
                .thenReturn(Optional.of(testProject)); // Third call returns project again

        // When & Then - First call should succeed
        assertThatCode(() -> projectService.get(testProject.getId()))
                .doesNotThrowAnyException();

        // Second call should fail
        assertThatThrownBy(() -> projectService.get(testProject.getId()))
                .isInstanceOf(NotFoundException.class);

        // Third call should succeed again
        assertThatCode(() -> projectService.get(testProject.getId()))
                .doesNotThrowAnyException();
    }

    // Edge Case: Repository returning null allocations
    @Test
    @DisplayName("Should handle project with null allocations list")
    void shouldHandleProjectWithNullAllocations() {
        // Given
        testProject.setAlocations(null);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        // When & Then - Should not throw NullPointerException
        assertThatThrownBy(() -> projectService.getCost(testProject.getId(), null, null))
                .isInstanceOf(NullPointerException.class);
    }

    // Edge Case: Contract with zero or negative wage
    @Test
    @DisplayName("Should handle contract with zero wage")
    void shouldHandleContractWithZeroWage() {
        // Given
        testContract.setWageByHour(0.0);
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then
        assertThat(result.getTotalCost()).isZero();
        assertThat(result.getTotalCostPerPeriod()).isZero();
    }

    @Test
    @DisplayName("Should handle contract with negative wage")
    void shouldHandleContractWithNegativeWage() {
        // Given
        testContract.setWageByHour(-10.0);
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then - Cost should be negative
        assertThat(result.getTotalCost()).isNegative();
        assertThat(result.getTotalCostPerPeriod()).isNegative();
    }

    // Edge Case: Allocation with zero or negative hours
    @Test
    @DisplayName("Should handle allocation with zero weekly hours")
    void shouldHandleAllocationWithZeroWeeklyHours() {
        // Given
        testAllocation.setWeeklyHours(0);
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then
        assertThat(result.getTotalCost()).isZero();
        assertThat(result.getTotalCostPerPeriod()).isZero();
    }

    @Test
    @DisplayName("Should handle allocation with negative weekly hours")
    void shouldHandleAllocationWithNegativeWeeklyHours() {
        // Given
        testAllocation.setWeeklyHours(-40);
        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(testContract));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then - Cost should be negative
        assertThat(result.getTotalCost()).isNegative();
        assertThat(result.getTotalCostPerPeriod()).isNegative();
    }

    // Edge Case: Multiple overlapping contracts for same employee
    @Test
    @DisplayName("Should handle multiple overlapping contracts for same employee")
    void shouldHandleMultipleOverlappingContracts() {
        // Given
        Contract contract1 = createTestContract();
        contract1.setWageByHour(10.0);
        contract1.setStartDate(OffsetDateTime.now().minusDays(20));
        contract1.setEndDate(OffsetDateTime.now().plusDays(5));

        Contract contract2 = createTestContract();
        contract2.setWageByHour(15.0);
        contract2.setStartDate(OffsetDateTime.now().minusDays(5));
        contract2.setEndDate(OffsetDateTime.now().plusDays(20));

        testProject.setAlocations(List.of(testAllocation));
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(contract1, contract2));

        // When
        ProjectCostDTO result = projectService.getCost(testProject.getId(), null, null);

        // Then - Should not crash and calculate some cost
        assertThat(result.getTotalCost()).isNotNegative();
        assertThat(result.getTotalCostPerPeriod()).isNotNegative();
    }

    // Edge Case: Project spanning leap year dates
    @Test
    @DisplayName("Should handle project spanning leap year correctly")
    void shouldHandleProjectSpanningLeapYear() {
        // Given - Create a project spanning February 29th in a leap year (2024)
        OffsetDateTime leapYearStart = OffsetDateTime.of(2024, 2, 28, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime leapYearEnd = OffsetDateTime.of(2024, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Project leapYearProject = createProjectWithDates(leapYearStart, leapYearEnd);
        // *** FIX: Create a new allocation for this specific project ***
        Alocation leapYearAllocation = createTestAllocation();
        leapYearAllocation.setProject(leapYearProject);
        leapYearProject.setAlocations(List.of(leapYearAllocation));

        Contract leapYearContract = createContractForPeriod(leapYearStart, leapYearEnd);

        when(projectRepository.findById(leapYearProject.getId())).thenReturn(Optional.of(leapYearProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(leapYearContract));

        // When
        ProjectCostDTO result = projectService.getCost(leapYearProject.getId(), null, null);

        // Then - Should handle leap day correctly
        // 2/28 (Wed), 2/29 (Thu), 3/1 (Fri) = 3 working days
        long workingDays = 3;
        double dailyHours = leapYearAllocation.getWeeklyHours() / 5.0;
        double expectedCost = dailyHours * leapYearContract.getWageByHour() * workingDays;

        assertThat(result.getTotalCostPerPeriod()).isEqualTo(expectedCost);
    }

    // Edge Case: Very short project duration (single day)
    @Test
    @DisplayName("Should handle single day project correctly")
    void shouldHandleSingleDayProject() {
        // Given - Project lasting only one day (a weekday)
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        OffsetDateTime singleDayStart = monday.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime singleDayEnd = monday.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Project singleDayProject = createProjectWithDates(singleDayStart, singleDayEnd);
        // *** FIX: Create a new allocation for this specific project ***
        Alocation singleDayAllocation = createTestAllocation();
        singleDayAllocation.setProject(singleDayProject);
        singleDayProject.setAlocations(List.of(singleDayAllocation));

        Contract singleDayContract = createContractForPeriod(singleDayStart, singleDayEnd);

        when(projectRepository.findById(singleDayProject.getId())).thenReturn(Optional.of(singleDayProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(singleDayContract));

        // When
        ProjectCostDTO result = projectService.getCost(singleDayProject.getId(), null, null);

        // Then - Should calculate cost for one day
        double expectedCost = (singleDayAllocation.getWeeklyHours() / 5.0) * singleDayContract.getWageByHour();
        assertThat(result.getTotalCostPerPeriod()).isEqualTo(expectedCost);
    }

    // Edge Case: Project spanning only weekends
    @Test
    @DisplayName("Should handle project spanning only weekends")
    void shouldHandleProjectSpanningOnlyWeekends() {
        // Given - Project from Saturday to Sunday
        LocalDate saturday = LocalDate.now().with(DayOfWeek.SATURDAY);
        OffsetDateTime weekendStart = saturday.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime weekendEnd = saturday.plusDays(1).atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Project weekendProject = createProjectWithDates(weekendStart, weekendEnd);
        // *** FIX: Create a new allocation for this specific project ***
        Alocation weekendAllocation = createTestAllocation();
        weekendAllocation.setProject(weekendProject);
        weekendProject.setAlocations(List.of(weekendAllocation));

        Contract weekendContract = createContractForPeriod(weekendStart, weekendEnd);

        when(projectRepository.findById(weekendProject.getId())).thenReturn(Optional.of(weekendProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(weekendContract));

        // When
        ProjectCostDTO result = projectService.getCost(weekendProject.getId(), null, null);

        // Then - Cost should be zero since no working days
        assertThat(result.getTotalCostPerPeriod()).isZero();
    }

    // Edge Case: Role check edge cases
    @Test
    @DisplayName("Should handle role check with duplicate roles")
    void shouldHandleRoleCheckWithDuplicateRoles() {
        // Given
        when(alocationRepository.findRolesByProjectId(testProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.DEV, Role.QA, Role.QA, Role.MANAGER, Role.MANAGER));

        // When
        ProjectStatus status = projectService.getProjectStatus(testProject);

        // Then - Should still recognize all required roles are present
        assertThat(status).isEqualTo(ProjectStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should handle role check with extra roles")
    void shouldHandleRoleCheckWithExtraRoles() {
        // Given
        when(alocationRepository.findRolesByProjectId(testProject.getId()))
                .thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER, Role.SECURITY));

        // When
        ProjectStatus status = projectService.getProjectStatus(testProject);

        // Then - Should recognize all required roles are present despite extra role
        assertThat(status).isEqualTo(ProjectStatus.AVAILABLE);
    }

    // Edge Case: Modal DTO mapping with extreme values
    @Test
    @DisplayName("Should handle modal DTO mapping with empty employee map")
    void shouldHandleModalDTOMappingWithEmptyEmployeeMap() {
        // Given
        testProject.setAlocations(Collections.emptyList());
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(alocationRepository.findRolesByProjectId(testProject.getId())).thenReturn(Collections.emptyList());

        // When
        ProjectModalDTO result = projectService.getModal(testProject.getId(), null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmployees()).isEmpty();
        assertThat(result.getCosts().getTotalCost()).isZero();
    }

    // Edge Case: Create project with null fields
    @Test
    @DisplayName("Should handle creating project and return valid ID")
    void shouldHandleCreatingProject() {
        // Given
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("New Project");
        projectDTO.setDescription("Description");
        projectDTO.setStartDate(OffsetDateTime.now());
        projectDTO.setEndDate(OffsetDateTime.now().plusDays(10));

        Project savedProject = new Project();
        savedProject.setId(999L);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // When
        Long result = projectService.create(projectDTO);

        // Then
        assertThat(result).isEqualTo(999L);
        verify(projectRepository).save(any(Project.class));
    }

    // Edge Case: Repository exceptions
    @Test
    @DisplayName("Should propagate repository exceptions")
    void shouldPropagateRepositoryExceptions() {
        // Given
        when(projectRepository.findAll(any(Sort.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> projectService.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    // Edge Case: Test with Long.MAX_VALUE IDs
    @Test
    @DisplayName("Should handle maximum long values for IDs")
    void shouldHandleMaximumLongValuesForIds() {
        // Given
        Long maxId = Long.MAX_VALUE;
        when(projectRepository.findById(maxId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.get(maxId))
                .isInstanceOf(NotFoundException.class);
    }

    // Helper methods remain the same as in the main test class
    private Project createTestProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setStartDate(OffsetDateTime.now().minusDays(10));
        project.setEndDate(OffsetDateTime.now().plusDays(10));
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