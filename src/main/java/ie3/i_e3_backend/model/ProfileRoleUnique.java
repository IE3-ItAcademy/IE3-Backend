package ie3.i_e3_backend.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import ie3.i_e3_backend.model.Enums.Role;
import ie3.i_e3_backend.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;


/**
 * Validate that the role value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = ProfileRoleUnique.ProfileRoleUniqueValidator.class
)
public @interface ProfileRoleUnique {

    String message() default "{Exists.profile.role}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ProfileRoleUniqueValidator implements ConstraintValidator<ProfileRoleUnique, Role> {

        private final ProfileService profileService;
        private final HttpServletRequest request;

        public ProfileRoleUniqueValidator(final ProfileService profileService,
                final HttpServletRequest request) {
            this.profileService = profileService;
            this.request = request;
        }

        @Override
        public boolean isValid(final Role value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("id");
            if (currentId != null && value.equals(profileService.get(Long.parseLong(currentId)).getRole())) {
                // value hasn't changed
                return true;
            }
            return !profileService.roleExists(value);
        }

    }

}
