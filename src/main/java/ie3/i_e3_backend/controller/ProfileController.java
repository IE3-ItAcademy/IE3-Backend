package ie3.i_e3_backend.controller;

import ie3.i_e3_backend.model.DTOs.ProfileDTO;
import ie3.i_e3_backend.service.ProfileService;
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
@RequestMapping(value = "/api/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(final ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<List<ProfileDTO>> getAllProfiles() {
        return ResponseEntity.ok(profileService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(profileService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createProfile(@RequestBody @Valid final ProfileDTO profileDTO) {
        final Long createdId = profileService.create(profileDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

}
