package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.ContractDTO;
import ie3.i_e3_backend.service.ContractService;
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
@RequestMapping(value = "/api/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractController {

    private final ContractService contractService;

    public ContractController(final ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public ResponseEntity<List<ContractDTO>> getAllContracts() {
        return ResponseEntity.ok(contractService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> getContract(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(contractService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createContract(@RequestBody @Valid final ContractDTO contractDTO) {
        final Long createdId = contractService.create(contractDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
