package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.EmployeeDTO;
import ie3.i_e3_backend.model.DTOs.EmployeeModalDTO;
import ie3.i_e3_backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
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

    @GetMapping("/details/{id}")
    public ResponseEntity<EmployeeModalDTO> getEmployeeDetails(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeDetails(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createEmployee(@RequestBody @Valid final EmployeeDTO employeeDTO) {
        final Long createdId = employeeService.create(employeeDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
