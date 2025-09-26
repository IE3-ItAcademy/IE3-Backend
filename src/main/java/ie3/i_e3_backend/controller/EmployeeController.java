package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.EmployeeDTO;
import ie3.i_e3_backend.model.DTOs.EmployeeModalDTO;
import ie3.i_e3_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/employees", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(final EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(employeeService.get(id));
    }

    @GetMapping("/modal/{id}")
    public ResponseEntity<EmployeeModalDTO> getEmployeeDetails(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeDetails(id));
    }

    @GetMapping("/getEmployeesWithWeeklyHoursForProject")
    public  ResponseEntity<List<EmployeeModalDTO>> getEmployeesWithWeeklyHoursForProject(@RequestParam final LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam final int weeklyHours) {
        return ResponseEntity.ok(employeeService.getEmployeesWithWeeklyHoursForProject(startDate, endDate, weeklyHours));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createEmployee(@RequestBody @Valid final EmployeeDTO employeeDTO) {
        final Long createdId = employeeService.create(employeeDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
