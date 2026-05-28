package app.budgetmanager.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonPropertyOrder({
    "id",
    "description",
    "amount",
    "date",
    "category",
    "walletName",
    "walletId",
    "userName",
    "userId",
    "tags"
})
@Schema(description = "Expense response")
public class ExpenseResponseDto extends ExpenseFieldsDto {

    @Schema(description = "Expense id", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Food")
    private String category;

    @Schema(description = "Wallet owner user id", example = "1")
    private Long userId;

    @Schema(description = "Wallet name", example = "Main")
    private String walletName;

    @Schema(description = "Wallet owner username", example = "john")
    private String userName;

    @Schema(description = "Tag names")
    private List<String> tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
