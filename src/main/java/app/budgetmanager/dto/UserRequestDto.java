package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration or update request")
public class UserRequestDto {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be at most 100 characters")
    @Schema(description = "Unique username", example = "john")
    private String username;

    @Size(max = 100, message = "Default wallet name must be at most 100 characters")
    @Schema(description = "Name for the default wallet created on registration", example = "Main")
    private String defaultWalletName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDefaultWalletName() {
        return defaultWalletName;
    }

    public void setDefaultWalletName(String defaultWalletName) {
        this.defaultWalletName = defaultWalletName;
    }
}
