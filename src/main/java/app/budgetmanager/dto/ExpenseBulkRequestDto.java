package app.budgetmanager.dto;

import app.budgetmanager.validation.ValidationGroups;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

import java.util.List;

@ArraySchema(schema = @Schema(implementation = ExpenseRequestDto.class))
@Schema(description = "JSON array of expenses for bulk create")
public class ExpenseBulkRequestDto {

    @NotEmpty(message = "Expense list must not be empty")
    @Valid
    @ConvertGroup(from = Default.class, to = ValidationGroups.FullValidation.class)
    private final List<ExpenseRequestDto> items;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ExpenseBulkRequestDto(List<ExpenseRequestDto> items) {
        this.items = items;
    }

    @JsonValue
    public List<ExpenseRequestDto> getItems() {
        return items;
    }
}
