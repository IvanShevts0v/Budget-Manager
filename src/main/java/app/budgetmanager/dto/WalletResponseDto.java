package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet response")
public class WalletResponseDto {

    @Schema(description = "Wallet id", example = "1")
    private Long id;

    @Schema(description = "Wallet name", example = "Main")
    private String name;

    @Schema(description = "Owner user id", example = "1")
    private Long userId;

    @Schema(description = "Owner username", example = "john")
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
