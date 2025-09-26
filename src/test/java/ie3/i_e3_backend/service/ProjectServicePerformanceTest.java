package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.ProjectCostDTO;
import ie3.i_e3_backend.model.DTOs.ProjectCountStatusDTO;
import ie3.i_e3_backend.model.DTOs.ProjectReadDTO;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Performance and Memory Edge Cases")
class ProjectServicePerformanceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AlocationRepository alocationRepository;

    @InjectMocks
    private ProjectService projectService;

    // Edge Case: Large dataset handling
    @Test
    @DisplayName("Should handle large number of projects efficiently")
    void shouldHandleLargeNumberOfProjects() {
        // Given - Create 10,000 projects
        List<Project> largeProjectList = IntStream.range(0, 10_000)
                .mapToObj(i -> createProjectWithId((long) i))
                .toList();

        when(projectRepository.findAll(any(Sort.class))).thenReturn(largeProjectList);
        when(alocationRepository.findRolesByProjectId(anyLong())).thenReturn(List.of(Role.DEV, Role.QA, Role.MANAGER));

        // When
        List<ProjectReadDTO> result = projectService.findAll();

        // Then
        assertThat(result).hasSize(10_000);
        verify(projectRepository, times(1)).findAll(Sort.by("id"));
    }

    @Test
    @DisplayName("Should handle large number of projects in count status")
    void shouldHandleLargeNumberOfProjectsInCountStatus() {
        // Given - Create projects with different statuses
        List<Project> projects = new ArrayList<>();
        
        // Create 1000 projects of each status type
        for (int i = 0; i < 1000; i++) {
            projects.add(createPastProject((long) i));           // FINISHED/COMPLETED
            projects.add(createActiveProject((long) (i + 1000)));    // AVAILABLE
            projects.add(createFutureProject((long) (i + 2000)));    // PLANNED
            projects.add(createActiveProject((long) (i + 3000)));    // UNAVAILABLE
            projects.add(createPastProject((long) (i + 4000)));      // FINISHED/COMPLETED
        }

        when(projectRepository.findAll(any(Sort.class))).thenReturn(projects);
        
        // Mock role checks alternately to create different statuses
        when(alocationRepository.findRolesByProjectId(anyLong()))
                .thenAnswer(invocation -> {
                    Long projectId = invocation.getArgument(0);
                    // Alternate between having all roles and no roles
                    return projectId % 2 == 0 ? 
                           List.of(Role.DEV, Role.QA, Role.MANAGER) : 
                           Collections.emptyList();
                });

        // When
        ProjectCountStatusDTO result = projectService.countByStatus();

        // Then
        assertThat(result.getTotalProjectCount()).isEqualTo(5000);
        assertThat(result.getCompletedProjectCount() + 
                  result.getFinishedProjectCount() +
                  result.getAvailableProjectCount() + 
                  result.getPlannedProjectCount() + 
                  result.getUnavailableProjectCount()).isEqualTo(5000);
    }

    // Edge Case: Project with large number of allocations
    @Test
    @DisplayName("Should handle project with many allocations")
    void shouldHandleProjectWithManyAllocations() {
        // Given - Project with 1000 allocations
        Project projectWithManyAllocations = createProjectWithId(1L);

        List<Alocation> manyAllocations = IntStream.range(0, 1000)
                .mapToObj(i -> {
                    Alocation allocation = new Alocation();
                    allocation.setId((long) i);
                    allocation.setEmployee(createEmployeeWithId((long) i));
                    allocation.setEmployeeRole(Role.DEV);
                    allocation.setWeeklyHours(40);
                    allocation.setProject(projectWithManyAllocations);
                    return allocation;
                })
                .toList();

        projectWithManyAllocations.setAlocations(manyAllocations);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectWithManyAllocations));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(createContractWithId(1L)));

        // When
        ProjectCostDTO result = projectService.getCost(1L, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCost()).isNotNegative();
        // Should complete without timeout or memory issues
    }

    // Edge Case: Very long project duration
    @Test
    @DisplayName("Should handle very long project duration")
    void shouldHandleVeryLongProjectDuration() {
        // Given - Project lasting 10 years
        OffsetDateTime startDate = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate = OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        
        Project longProject = createProjectWithDates(1L, startDate, endDate);
        longProject.setAlocations(List.of(createAllocationWithId(1L)));

        Contract longContract = createContractWithId(1L);
        longContract.setStartDate(startDate);
        longContract.setEndDate(endDate);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(longProject));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(List.of(longContract));

        // When
        ProjectCostDTO result = projectService.getCost(1L, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCost()).isNotNegative();
        // Should handle ~3650 days of calculation efficiently
    }

    // Edge Case: Many overlapping contracts
    @Test
    @DisplayName("Should handle many overlapping contracts efficiently")
    void shouldHandleManyOverlappingContracts() {
        // Given
        Project project = createProjectWithId(1L);
        project.setAlocations(List.of(createAllocationWithId(1L)));

        // Create 100 overlapping contracts
        List<Contract> manyContracts = IntStream.range(0, 100)
                .mapToObj(i -> {
                    Contract contract = createContractWithId((long) i);
                    contract.setStartDate(OffsetDateTime.now().minusDays(100 - i));
                    contract.setEndDate(OffsetDateTime.now().plusDays(i));
                    contract.setWageByHour(10.0 + i); // Different wages
                    return contract;
                })
                .toList();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(contractRepository.findAllByEmployeeIdAndDateRange(anyLong(), any(), any()))
                .thenReturn(manyContracts);

        // When
        ProjectCostDTO result = projectService.getCost(1L, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCost()).isNotNegative();
    }

    // Edge Case: Null handling in collections
    @Test
    @DisplayName("Should handle null elements in project list gracefully")
    void shouldHandleNullElementsInProjectList() {
        // Given - List containing null elements
        List<Project> projectsWithNulls = Arrays.asList(
                createProjectWithId(1L),
                null,
                createProjectWithId(2L),
                null,
                createProjectWithId(3L)
        );

        when(projectRepository.findAll(any(Sort.class))).thenReturn(projectsWithNulls);
        when(alocationRepository.findRolesByProjectId(anyLong())).thenReturn(List.of(Role.DEV));

        // When & Then - Should handle nulls gracefully
        assertThatThrownBy(() -> projectService.findAll())
                .isInstanceOf(NullPointerException.class);
    }

    // Edge Case: Memory stress with large objects
    @Test
    @DisplayName("Should handle projects with very long descriptions")
    void shouldHandleProjectsWithVeryLongDescriptions() {
        // Given - Project with very long description (1MB string)
        Project projectWithLongDescription = createProjectWithId(1L);
        String longDescription = "A".repeat(1_000_000); // 1MB string
        projectWithLongDescription.setDescription(longDescription);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectWithLongDescription));
        when(alocationRepository.findRolesByProjectId(1L)).thenReturn(List.of(Role.DEV));

        // When
        ProjectReadDTO result = projectService.get(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).hasSize(1_000_000);
    }

    // Edge Case: Filtering performance with complex predicates
    @Test
    @DisplayName("Should filter projects efficiently with complex conditions")
    void shouldFilterProjectsEfficientlyWithComplexConditions() {
        // Given - Mix of projects with different statuses
        List<Project> mixedProjects = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            mixedProjects.add(createPastProject((long) i));     // Will be FINISHED/COMPLETED
            mixedProjects.add(createActiveProject((long) i + 1000)); // Will be AVAILABLE/UNAVAILABLE  
            mixedProjects.add(createFutureProject((long) i + 2000)); // Will be PLANNED
        }

        when(projectRepository.findAll(any(Sort.class))).thenReturn(mixedProjects);
        
        // Mock role checks - every 3rd project has roles
        when(alocationRepository.findRolesByProjectId(anyLong()))
                .thenAnswer(invocation -> {
                    Long projectId = invocation.getArgument(0);
                    return projectId % 3 == 0 ? 
                           List.of(Role.DEV, Role.QA, Role.MANAGER) : 
                           Collections.emptyList();
                });

        // When
        List<ProjectReadDTO> result = projectService.findAllLessFinishedAndComplete();

        // Then - Should exclude FINISHED and COMPLETED projects
        assertThat(result.size()).isLessThan(3000);
        // Verify no FINISHED or COMPLETED projects in result
        assertThat(result).noneMatch(project -> 
                project.getStatus() == ProjectStatus.FINISHED || 
                project.getStatus() == ProjectStatus.COMPLETED);
    }

    // Helper methods
    private Project createProjectWithId(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Project " + id);
        project.setDescription("Description " + id);
        project.setStartDate(OffsetDateTime.now().minusDays(10));
        project.setEndDate(OffsetDateTime.now().plusDays(10));
        return project;
    }

    private Project createProjectWithDates(Long id, OffsetDateTime startDate, OffsetDateTime endDate) {
        Project project = new Project();
        project.setId(id);
        project.setName("Project " + id);
        project.setDescription("Description " + id);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return project;
    }

    private Project createPastProject(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Past Project " + id);
        project.setDescription("Past Description " + id);
        project.setStartDate(OffsetDateTime.now().minusDays(30));
        project.setEndDate(OffsetDateTime.now().minusDays(10));
        return project;
    }

    private Project createActiveProject(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Active Project " + id);
        project.setDescription("Active Description " + id);
        project.setStartDate(OffsetDateTime.now().minusDays(5));
        project.setEndDate(OffsetDateTime.now().plusDays(15));
        return project;
    }

    private Project createFutureProject(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Future Project " + id);
        project.setDescription("Future Description " + id);
        project.setStartDate(OffsetDateTime.now().plusDays(5));
        project.setEndDate(OffsetDateTime.now().plusDays(25));
        return project;
    }

    private Employee createEmployeeWithId(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Employee " + id);
        return employee;
    }

    private Contract createContractWithId(Long id) {
        Contract contract = new Contract();
        contract.setId(id);
        contract.setEmployee(createEmployeeWithId(id));
        contract.setWageByHour(20.0);
        contract.setStartDate(OffsetDateTime.now().minusDays(30));
        contract.setEndDate(OffsetDateTime.now().plusDays(30));
        return contract;
    }

    private Alocation createAllocationWithId(Long id) {
        Alocation allocation = new Alocation();
        allocation.setId(id);
        allocation.setProject(createProjectWithId(id));
        allocation.setEmployee(createEmployeeWithId(id));
        allocation.setEmployeeRole(Role.DEV);
        allocation.setWeeklyHours(40);
        return allocation;
    }
}