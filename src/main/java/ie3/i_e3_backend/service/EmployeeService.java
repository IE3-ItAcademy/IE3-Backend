package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Alocation;
import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.domain.Project;
import ie3.i_e3_backend.model.DTOs.EmployeeDTO;
import ie3.i_e3_backend.model.DTOs.EmployeeModalDTO;
import ie3.i_e3_backend.model.DTOs.ProjectInfoDTO;
import ie3.i_e3_backend.repos.ContractRepository;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;


@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ProjectService projectService;

    private final ApplicationEventPublisher publisher;

    public EmployeeService(final EmployeeRepository employeeRepository, ContractRepository contractRepository, ProjectService projectService,
                           final ApplicationEventPublisher publisher) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.projectService = projectService;
        this.publisher = publisher;
    }

    public List<EmployeeDTO> findAll() {
        final List<Employee> employees = employeeRepository.findAll(Sort.by("id"));
        return employees.stream()
                .map(employee -> mapToDTO(employee, new EmployeeDTO()))
                .toList();
    }

    public EmployeeDTO get(final Long id) {
        return employeeRepository.findById(id)
                .map(employee -> mapToDTO(employee, new EmployeeDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public EmployeeModalDTO getEmployeeDetails(final Long id) {
       OffsetDateTime now = OffsetDateTime.now();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        var currentContract = contractRepository.findTopActiveContractByEmployeeId(id, now)
                .stream().findFirst().orElse(null);

        return mapToModalDTO(employee, new EmployeeModalDTO(), currentContract != null);
    }

    public Long create(final EmployeeDTO employeeDTO) {
        final Employee employee = new Employee();
        mapToEntity(employeeDTO, employee);
        return employeeRepository.save(employee).getId();
    }

    private EmployeeDTO mapToDTO(final Employee employee, final EmployeeDTO employeeDTO) {
        employeeDTO.setId(employee.getId());
        employeeDTO.setName(employee.getName());
        return employeeDTO;
    }

    private Employee mapToEntity(final EmployeeDTO employeeDTO, final Employee employee) {
        employee.setName(employeeDTO.getName());
        return employee;
    }

   private EmployeeModalDTO mapToModalDTO(final Employee employee, final EmployeeModalDTO employeeModalDTO, final boolean activeContract) {
        employeeModalDTO.setId(employee.getId());
        employeeModalDTO.setName(employee.getName());
        employeeModalDTO.setActiveContract(activeContract);

        List<Project> projects = employee.getAlocations().stream().map(Alocation::getProject).toList();
        List<ProjectInfoDTO> projectsDTO = projects.stream()
               .map(p -> new ProjectInfoDTO(p.getName(), projectService.getProjectStatus(p)))
               .toList();

        employeeModalDTO.setContractInfoList(projectsDTO);

        return employeeModalDTO;
   }
}
