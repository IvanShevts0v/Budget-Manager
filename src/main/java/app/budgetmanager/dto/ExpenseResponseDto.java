package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDto(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        LocalDate date
) {
}
