package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDto {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String category;
    private LocalDate date;
}
