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
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }
}
