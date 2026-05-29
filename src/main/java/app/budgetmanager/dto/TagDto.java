package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tag create or update request")
public class TagDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Schema(description = "Tag name", example = "travel")
    private String name;
}
