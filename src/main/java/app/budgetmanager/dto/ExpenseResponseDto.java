package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseResponseDto(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        LocalDate date,
        Long walletId,
        Long userId,
        List<String> tags
) {
}
