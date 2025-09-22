package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.AlocationDTO;
import ie3.i_e3_backend.service.AlocationService;
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
@RequestMapping(value = "/api/alocations", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlocationController {

    private final AlocationService alocationService;

    public AlocationController(final AlocationService alocationService) {
        this.alocationService = alocationService;
    }

    @GetMapping
    public ResponseEntity<List<AlocationDTO>> getAllAlocations() {
        return ResponseEntity.ok(alocationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlocationDTO> getAlocation(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(alocationService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createAlocation(
            @RequestBody @Valid final AlocationDTO alocationDTO) {
        final Long createdId = alocationService.create(alocationDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
