package app.budgetmanager.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос демо: сохранение связанных User и Wallet с опциональным сбоем после кошелька.
 */
public record DemoRelatedSaveRequest(
        @NotBlank String username,
        @NotBlank String walletName,
        boolean failAfterWallet
) {
}
