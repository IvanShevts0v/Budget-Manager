package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseSearchCriteria(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        LocalDate date,
        Long walletOwnerUserId,
        List<String> tagNames
) {
}
