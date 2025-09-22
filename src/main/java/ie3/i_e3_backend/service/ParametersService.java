package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Parameters;
import ie3.i_e3_backend.model.DTOs.ParametersDTO;
import ie3.i_e3_backend.repos.ParametersRepository;
import ie3.i_e3_backend.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class ParametersService {

    private final ParametersRepository parametersRepository;

    public ParametersService(final ParametersRepository parametersRepository) {
        this.parametersRepository = parametersRepository;
    }

    public List<ParametersDTO> findAll() {
        final List<Parameters> parameterses = parametersRepository.findAll(Sort.by("id"));
        return parameterses.stream()
                .map(parameters -> mapToDTO(parameters, new ParametersDTO()))
                .toList();
    }

    public ParametersDTO get(final Long id) {
        return parametersRepository.findById(id)
                .map(parameters -> mapToDTO(parameters, new ParametersDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final ParametersDTO parametersDTO) {
        final Parameters parameters = new Parameters();
        mapToEntity(parametersDTO, parameters);
        return parametersRepository.save(parameters).getId();
    }

    public void update(final Long id, final ParametersDTO parametersDTO) {
        final Parameters parameters = parametersRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(parametersDTO, parameters);
        parametersRepository.save(parameters);
    }

    public void delete(final Long id) {
        final Parameters parameters = parametersRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        parametersRepository.delete(parameters);
    }

    private ParametersDTO mapToDTO(final Parameters parameters, final ParametersDTO parametersDTO) {
        parametersDTO.setId(parameters.getId());
        parametersDTO.setDescription(parameters.getDescription());
        parametersDTO.setValue(parameters.getValue());
        return parametersDTO;
    }

    private Parameters mapToEntity(final ParametersDTO parametersDTO, final Parameters parameters) {
        parameters.setDescription(parametersDTO.getDescription());
        parameters.setValue(parametersDTO.getValue());
        return parameters;
    }

}
