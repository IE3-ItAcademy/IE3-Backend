package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.ProjectDTO;
import ie3.i_e3_backend.repos.ProjectRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher publisher;

    public ProjectService(final ProjectRepository projectRepository,
            final ApplicationEventPublisher publisher) {
        this.projectRepository = projectRepository;
        this.publisher = publisher;
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
