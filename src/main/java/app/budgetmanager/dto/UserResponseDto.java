package app.budgetmanager.dto;

import java.util.List;

public class UserResponseDto {

    private Long id;
    private String username;
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
}
