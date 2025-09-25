package ie3.i_e3_backend.model.Enums;

import ie3.i_e3_backend.domain.Project;

import java.time.OffsetDateTime;

public enum ProjectStatus {
    FINISHED,       // No roles, after end
    UNAVAILABLE,    // No roles, not started or active
    COMPLETED,      // Has roles, after end
    PLANNED,        // Has roles, before start
    AVAILABLE,      // Has roles, currently active
    UNKNOWN;        // Fallback

    public static ProjectStatus evaluate(Project project, boolean hasRoles) {
        OffsetDateTime now = OffsetDateTime.now();
        boolean isBeforeStart = project.getStartDate().isAfter(now);
        boolean isAfterEnd = project.getEndDate().isBefore(now);
        boolean isActive = !isBeforeStart && !isAfterEnd;

        if (!hasRoles && isAfterEnd) return FINISHED;
        if (!hasRoles) return UNAVAILABLE;
        if (isAfterEnd) return COMPLETED;
        if (isBeforeStart) return PLANNED;
        if (isActive) return AVAILABLE;

        return UNKNOWN;
    }
}
