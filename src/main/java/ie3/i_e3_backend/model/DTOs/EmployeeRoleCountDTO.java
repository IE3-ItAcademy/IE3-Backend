package ie3.i_e3_backend.model.DTOs;

public class EmployeeRoleCountDTO {
    private int totalEmployeeCountWithActiveContracts;
    private int managerCount;
    private int devCount;
    private int qaCount;
    private int securityCount;

    public int getManagerCount() {
        return managerCount;
    }

    public int getTotalEmployeeCountWithActiveContracts() {
        return totalEmployeeCountWithActiveContracts;
    }

    public void setTotalEmployeeCountWithActiveContracts(int totalEmployeeCountWithActiveContracts) {
        this.totalEmployeeCountWithActiveContracts = totalEmployeeCountWithActiveContracts;
    }

    public void setManagerCount(int managerCount) {
        this.managerCount = managerCount;
    }

    public int getDevCount() {
        return devCount;
    }

    public void setDevCount(int devCount) {
        this.devCount = devCount;
    }

    public int getQaCount() {
        return qaCount;
    }

    public void setQaCount(int qaCount) {
        this.qaCount = qaCount;
    }

    public int getSecurityCount() {
        return securityCount;
    }

    public void setSecurityCount(int securityCount) {
        this.securityCount = securityCount;
    }
}
