package ie3.i_e3_backend.model.DTOs;

import ie3.i_e3_backend.model.Enums.Role;

import java.util.List;

public class EmployeeModalDTO {
    private Long id;
    private String name;
    private boolean activeContract;
    private List<Role> roles;
    private List<ProjectInfoDTO> projectInfoList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActiveContract() {
        return activeContract;
    }

    public void setActiveContract(boolean activeContract) {
        this.activeContract = activeContract;
    }

    public List<ProjectInfoDTO> getProjectInfoList() {
        return projectInfoList;
    }

    public void setContractInfoList(List<ProjectInfoDTO> contractInfoList) {
        this.projectInfoList = contractInfoList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Role> getRoles() {return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; };
}
