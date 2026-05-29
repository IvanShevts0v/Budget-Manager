package app.budgetmanager.dto;

import app.budgetmanager.validation.ValidationGroups;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wallet create or update request")
public class WalletRequestDto {

    @NotNull(groups = ValidationGroups.FullValidation.class, message = "User id is required")
    @Schema(description = "Owner user id", example = "1")
    private Long userId;

    @NotBlank(
            groups = {ValidationGroups.FullValidation.class, ValidationGroups.RenameValidation.class},
            message = "Wallet name is required"
    )
    @Size(
            groups = {ValidationGroups.FullValidation.class, ValidationGroups.RenameValidation.class},
            max = 100,
            message = "Wallet name must be at most 100 characters"
    )
    @Schema(description = "Wallet name", example = "Main")
    private String name;
}
