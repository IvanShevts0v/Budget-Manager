package app.budgetmanager.dto;

public class UserRequestDto {

    private String username;
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
