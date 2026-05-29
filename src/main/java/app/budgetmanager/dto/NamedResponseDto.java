package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entity with id and name")
public class NamedResponseDto {

    @Schema(description = "Entity id", example = "1")
    private Long id;

    @Schema(description = "Entity name", example = "Food")
    private String name;
}
