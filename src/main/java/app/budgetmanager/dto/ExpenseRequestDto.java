package app.budgetmanager.dto;

import java.util.List;

public class ExpenseRequestDto extends ExpenseFieldsDto {

    private Long categoryId;
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
