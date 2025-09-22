package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.ParametersDTO;
import ie3.i_e3_backend.service.ParametersService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/parameterss", produces = MediaType.APPLICATION_JSON_VALUE)
public class ParametersController {

    private final ParametersService parametersService;

    public ParametersController(final ParametersService parametersService) {
        this.parametersService = parametersService;
    }

    @GetMapping
    public ResponseEntity<List<ParametersDTO>> getAllParameterss() {
        return ResponseEntity.ok(parametersService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParametersDTO> getParameters(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(parametersService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createParameters(
            @RequestBody @Valid final ParametersDTO parametersDTO) {
        final Long createdId = parametersService.create(parametersDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateParameters(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final ParametersDTO parametersDTO) {
        parametersService.update(id, parametersDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteParameters(@PathVariable(name = "id") final Long id) {
        parametersService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
