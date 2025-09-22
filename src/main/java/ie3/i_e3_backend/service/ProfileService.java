package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Profile;
import ie3.i_e3_backend.model.DTOs.ProfileDTO;
import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.repos.ProfileRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher publisher;

    public ProfileService(final ProfileRepository profileRepository,
            final ApplicationEventPublisher publisher) {
        this.profileRepository = profileRepository;
        this.publisher = publisher;
    }

    public List<ProfileDTO> findAll() {
        final List<Profile> profiles = profileRepository.findAll(Sort.by("id"));
        return profiles.stream()
                .map(profile -> mapToDTO(profile, new ProfileDTO()))
                .toList();
    }

    public ProfileDTO get(final Long id) {
        return profileRepository.findById(id)
                .map(profile -> mapToDTO(profile, new ProfileDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final ProfileDTO profileDTO) {
        final Profile profile = new Profile();
        mapToEntity(profileDTO, profile);
        return profileRepository.save(profile).getId();
    }

    private ProfileDTO mapToDTO(final Profile profile, final ProfileDTO profileDTO) {
        profileDTO.setId(profile.getId());
        profileDTO.setRole(profile.getRole());
        return profileDTO;
    }

    private Profile mapToEntity(final ProfileDTO profileDTO, final Profile profile) {
        profile.setRole(profileDTO.getRole());
        return profile;
    }

    public boolean roleExists(final Role role) {
        return profileRepository.existsByRole(role);
    }

}
