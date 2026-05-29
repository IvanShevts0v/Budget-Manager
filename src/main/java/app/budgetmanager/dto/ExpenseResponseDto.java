package app.budgetmanager.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Getter
@Setter
@NoArgsConstructor
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
}
