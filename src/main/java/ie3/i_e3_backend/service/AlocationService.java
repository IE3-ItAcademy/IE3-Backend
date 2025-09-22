package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.AlocationDTO;
import ie3.i_e3_backend.repos.AlocationRepository;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.repos.ProjectRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AlocationService {

    private final AlocationRepository alocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public AlocationService(final AlocationRepository alocationRepository,
            final EmployeeRepository employeeRepository,
            final ProjectRepository projectRepository) {
        this.alocationRepository = alocationRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
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

    public Long create(final AlocationDTO alocationDTO) {
        final Alocation alocation = new Alocation();
        mapToEntity(alocationDTO, alocation);
        return alocationRepository.save(alocation).getId();
    }

    private AlocationDTO mapToDTO(final Alocation alocation, final AlocationDTO alocationDTO) {
        alocationDTO.setId(alocation.getId());
        alocationDTO.setWeeklyHours(alocation.getWeeklyHours());
        alocationDTO.setEmployeeRole(alocation.getEmployeeRole());
        alocationDTO.setUser(alocation.getUser() == null ? null : alocation.getUser().getId());
        alocationDTO.setProject(alocation.getProject() == null ? null : alocation.getProject().getId());
        return alocationDTO;
    }

    private Alocation mapToEntity(final AlocationDTO alocationDTO, final Alocation alocation) {
        alocation.setWeeklyHours(alocationDTO.getWeeklyHours());
        alocation.setEmployeeRole(alocationDTO.getEmployeeRole());
        final Employee user = alocationDTO.getUser() == null ? null : employeeRepository.findById(alocationDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        alocation.setUser(user);
        final Project project = alocationDTO.getProject() == null ? null : projectRepository.findById(alocationDTO.getProject())
                .orElseThrow(() -> new NotFoundException("project not found"));
        alocation.setProject(project);
        return alocation;
    }

    public boolean userExists(final Long id) {
        return alocationRepository.existsByUserId(id);
    }

    public boolean projectExists(final Long id) {
        return alocationRepository.existsByProjectId(id);
    }

}
