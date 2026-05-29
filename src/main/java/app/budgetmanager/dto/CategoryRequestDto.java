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
@Schema(description = "Category create or update request")
public class CategoryRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    @Schema(description = "Category name", example = "Food")
    private String name;
}
