package ie3.i_e3_backend.util;

public class ExistentManagerException extends RuntimeException {
    private final Long projectId;

    public ExistentManagerException(Long projectId) {
        super(String.format("The project with id %d already has a manager.", projectId));
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }
}