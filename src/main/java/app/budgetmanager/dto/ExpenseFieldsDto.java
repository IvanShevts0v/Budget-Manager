package app.budgetmanager.dto;

import app.budgetmanager.validation.ValidationGroups;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Common expense fields")
public abstract class ExpenseFieldsDto {

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(description = "Expense description", example = "Groceries")
    private String description;

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "Amount is required")
    @DecimalMin(
            groups = ValidationGroups.FullValidation.class,
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    @Schema(description = "Expense amount", example = "42.50")
    private BigDecimal amount;

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "Date is required")
    @Schema(description = "Expense date", example = "2026-05-27")
    private LocalDate date;

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "Wallet id is required")
    @Schema(description = "Wallet id", example = "1")
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
