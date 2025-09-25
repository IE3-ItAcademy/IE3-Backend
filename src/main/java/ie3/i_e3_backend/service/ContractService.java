package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Contract;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Profile;
import ie3.i_e3_backend.model.DTOs.ContractDTO;
import ie3.i_e3_backend.model.DTOs.ContractReadDTO;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.repos.ProfileRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    public List<ContractReadDTO> findAll() {
        final List<Contract> contracts = contractRepository.findAll(Sort.by("id"));
        return contracts.stream()
                .map(contract -> mapToDTO(contract, new ContractReadDTO()))
                .toList();
    }

    public ContractReadDTO get(final Long id) {
        return contractRepository.findById(id)
                .map(contract -> mapToDTO(contract, new ContractReadDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public List<ContractReadDTO> findAllByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return contractRepository.findAllByDateRange(startDate, endDate).stream()
                .map(contract -> mapToDTO(contract, new ContractReadDTO()))
                .toList();
    }

    public Long create(final ContractDTO contractDTO) {
        final Contract contract = new Contract();
        mapToEntity(contractDTO, contract);
        return contractRepository.save(contract).getId();
    }

    private boolean getContractStatus(Contract contract) {
        return contractRepository
                .findTopActiveContractByEmployeeId(contract.getEmployee().getId(), OffsetDateTime.now())
                .map(activeContract -> activeContract.equals(contract))
                .orElse(false);
    }

    private ContractReadDTO mapToDTO(final Contract contract, final ContractReadDTO contractReadDTO) {
        contractReadDTO.setId(contract.getId());
        contractReadDTO.setName(contract.getEmployee().getName());
        contractReadDTO.setStartDate(contract.getStartDate());
        contractReadDTO.setEndDate(contract.getEndDate());
        contractReadDTO.setWeeklyHours(contract.getWeeklyHours());
        contractReadDTO.setWageByHour(contract.getWageByHour());
        contractReadDTO.setName(contract.getEmployee().getName());
        contractReadDTO.setEmployeeId(contract.getEmployee().getId());
        contractReadDTO.setActiveContract(getContractStatus(contract));
        contractReadDTO.setProfile(contract.getProfile().stream()
                .map(profile -> profile.getId())
                .toList());
        return contractReadDTO;
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
