package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.*;
import ie3.i_e3_backend.service.ProjectService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;


@RestController
@RequestMapping(value = "/api/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectReadDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping("/countByStatus")
    public ResponseEntity<ProjectCountStatusDTO> getAllProjectsByStatus() {return ResponseEntity.ok(projectService.countByStatus()); }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectReadDTO> getProject(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @GetMapping("/cost/{id}")
    public ResponseEntity<ProjectCostDTO> getProjectCost(
            @PathVariable(name = "id") final Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate
    ) {
        return ResponseEntity.ok(projectService.getCost(id, startDate, endDate));
    }

    @GetMapping("/modal/{id}")
    public ResponseEntity<ProjectModalDTO> getProjectModal(
            @PathVariable(name = "id") final Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate
    ) {
        return ResponseEntity.ok(projectService.getModal(id, startDate, endDate));
    }

    @GetMapping("/dateRange")
    public ResponseEntity<List<ProjectReadDTO>> getContractsByDateRange(
            @RequestParam("startDate") OffsetDateTime startDate,
            @RequestParam("endDate") OffsetDateTime endDate) {

        return ResponseEntity.ok(projectService.findAllByDateRange(startDate, endDate)) ;
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createProject(@RequestBody @Valid final ProjectDTO projectDTO) {
        final Long createdId = projectService.create(projectDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
