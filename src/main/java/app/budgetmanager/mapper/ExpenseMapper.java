package app.budgetmanager.mapper;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setDate(expense.getDate());
        return dto;
    }
}
