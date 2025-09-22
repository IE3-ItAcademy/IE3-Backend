package ie3.i_e3_backend.model.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class ParametersDTO {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String description;

    @NotNull
    @Size(max = 255)
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

}
