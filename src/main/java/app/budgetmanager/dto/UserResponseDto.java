package app.budgetmanager.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonPropertyOrder({
    "id",
    "username",
    "walletNames",
    "walletIds"
})
@Schema(description = "User response")
public class UserResponseDto {

    @Schema(description = "User id", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john")
    private String username;

    @Schema(description = "Wallet names owned by the user")
    private List<String> walletNames;

    @Schema(description = "Wallet ids owned by the user")
    private List<Long> walletIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Long> getWalletIds() {
        return walletIds;
    }

    public void setWalletIds(List<Long> walletIds) {
        this.walletIds = walletIds;
    }

    public List<String> getWalletNames() {
        return walletNames;
    }

    public void setWalletNames(List<String> walletNames) {
        this.walletNames = walletNames;
    }
}
