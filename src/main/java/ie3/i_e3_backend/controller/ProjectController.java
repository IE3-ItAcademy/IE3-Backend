package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.ProjectCostDTO;
import ie3.i_e3_backend.model.DTOs.ProjectDTO;
import ie3.i_e3_backend.service.ProjectService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @GetMapping("/cost/{id}")
    public ResponseEntity<ProjectCostDTO> getProjectCost(@PathVariable(name = "id") final Long id, @PathVariable final OffsetDateTime startDate, @PathVariable final OffsetDateTime endDate) {
        return ResponseEntity.ok(projectService.getCost(id, startDate, endDate));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createProject(@RequestBody @Valid final ProjectDTO projectDTO) {
        final Long createdId = projectService.create(projectDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
