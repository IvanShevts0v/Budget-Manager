package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Общие поля расхода для запроса и ответа API (снижает дублирование между DTO).
 */
public abstract class ExpenseFieldsDto {

    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private Long walletId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }
}
