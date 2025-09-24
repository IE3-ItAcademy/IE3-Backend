package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Profile;
import ie3.i_e3_backend.model.DTOs.ContractDTO;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.repos.ProfileRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class)
public class ContractService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ProfileRepository profileRepository;

    public ContractService(final ContractRepository contractRepository,
            final EmployeeRepository employeeRepository,
            final ProfileRepository profileRepository) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.profileRepository = profileRepository;
    }

    public List<ContractDTO> findAll() {
        final List<Contract> contracts = contractRepository.findAll(Sort.by("id"));
        return contracts.stream()
                .map(contract -> mapToDTO(contract, new ContractDTO()))
                .toList();
    }

    public ContractDTO get(final Long id) {
        return contractRepository.findById(id)
                .map(contract -> mapToDTO(contract, new ContractDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final ContractDTO contractDTO) {
        final Contract contract = new Contract();
        mapToEntity(contractDTO, contract);
        return contractRepository.save(contract).getId();
    }

    private ContractDTO mapToDTO(final Contract contract, final ContractDTO contractDTO) {
        contractDTO.setId(contract.getId());
        contractDTO.setEmployeeName(contract.getEmployee().getName());
        contractDTO.setStartDate(contract.getStartDate());
        contractDTO.setEndDate(contract.getEndDate());
        contractDTO.setWeeklyHours(contract.getWeeklyHours());
        contractDTO.setWageByHour(contract.getWageByHour());
        contractDTO.setEmployeeName(contract.getEmployee().getName());
        contractDTO.setEmployeeId(contract.getEmployee().getId());
        contractDTO.setProfile(contract.getProfile().stream()
                .map(profile -> profile.getId())
                .toList());
        return contractDTO;
    }

    private Contract mapToEntity(final ContractDTO contractDTO, final Contract contract) {
        contract.setStartDate(contractDTO.getStartDate());
        contract.setEndDate(contractDTO.getEndDate());
        contract.setWeeklyHours(contractDTO.getWeeklyHours());
        contract.setWageByHour(contractDTO.getWageByHour());
        final Employee employee = contractDTO.getEmployeeId() == null ? null : employeeRepository.findById(contractDTO.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("users not found"));
        contract.setEmployee(employee);
        final List<Profile> profile = profileRepository.findAllById(
                contractDTO.getProfile() == null ? List.of() : contractDTO.getProfile());
        if (profile.size() != (contractDTO.getProfile() == null ? 0 : contractDTO.getProfile().size())) {
            throw new NotFoundException("one of profile not found");
        }
        contract.setProfile(new HashSet<>(profile));
        return contract;
    }

}
