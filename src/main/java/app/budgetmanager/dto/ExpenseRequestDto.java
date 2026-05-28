package app.budgetmanager.dto;

import app.budgetmanager.validation.ValidationGroups;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Expense create or update request")
public class ExpenseRequestDto extends ExpenseFieldsDto {

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "Category id is required")
    @Schema(description = "Category id", example = "2")
    private Long categoryId;

    @Schema(description = "Tag ids attached to the expense", example = "[1, 2]")
    private List<Long> tagIds;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
