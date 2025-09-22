package ie3.i_e3_backend.service;

import ie3.i_e3_backend.domain.Employee;
import ie3.i_e3_backend.model.DTOs.EmployeeDTO;
import ie3.i_e3_backend.repos.EmployeeRepository;
import ie3.i_e3_backend.util.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher publisher;

    public EmployeeService(final EmployeeRepository employeeRepository,
            final ApplicationEventPublisher publisher) {
        this.employeeRepository = employeeRepository;
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

}
