package app.budgetmanager.mapper;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.entity.Expense;
import app.budgetmanager.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ExpenseMapper {

    public ExpenseResponseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }
        String categoryName = expense.getCategory() != null ? expense.getCategory().getName() : null;
        Long walletId = expense.getWallet() != null ? expense.getWallet().getId() : null;
        Long userId = expense.getWallet() != null && expense.getWallet().getUser() != null
                ? expense.getWallet().getUser().getId()
                : null;
        List<String> tagNames = expense.getTags() == null
                ? List.of()
                : expense.getTags().stream()
                .map(Tag::getName)
                .sorted(Comparator.naturalOrder())
                .toList();
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                categoryName,
                expense.getDate(),
                walletId,
                userId,
                tagNames
        );
    }
}
