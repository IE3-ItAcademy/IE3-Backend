package ie3.i_e3_backend.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import ie3.i_e3_backend.service.AlocationService;
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
 * Validate that the user value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = AlocationUserUnique.AlocationUserUniqueValidator.class
)
public @interface AlocationUserUnique {

    String message() default "{Exists.alocation.user}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class AlocationUserUniqueValidator implements ConstraintValidator<AlocationUserUnique, Long> {

        private final AlocationService alocationService;
        private final HttpServletRequest request;

        public AlocationUserUniqueValidator(final AlocationService alocationService,
                final HttpServletRequest request) {
            this.alocationService = alocationService;
            this.request = request;
        }

        @Override
        public boolean isValid(final Long value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked") final Map<String, String> pathVariables =
                    ((Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("id");
            if (currentId != null && value.equals(alocationService.get(Long.parseLong(currentId)).getUser())) {
                // value hasn't changed
                return true;
            }
            return !alocationService.userExists(value);
        }

    }

}
