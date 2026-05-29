package app.budgetmanager.dto;

import app.budgetmanager.validation.ValidationGroups;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Expense create or update request")
public class ExpenseRequestDto extends ExpenseFieldsDto {

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "Category id is required")
    @Schema(description = "Category id", example = "2")
    private Long categoryId;

    @Schema(description = "Tag ids attached to the expense", example = "[1, 2]")
    private List<Long> tagIds;
}
