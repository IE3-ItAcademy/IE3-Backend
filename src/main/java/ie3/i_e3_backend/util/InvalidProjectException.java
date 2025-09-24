package ie3.i_e3_backend.util;

public class InvalidProjectException extends RuntimeException {
    private final Long projectId;

    public InvalidProjectException(Long projectId) {
        super(String.format("The project with id %d is no longer avaliable.", projectId));
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }
}
