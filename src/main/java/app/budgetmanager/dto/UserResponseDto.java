package app.budgetmanager.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonPropertyOrder({
    "id",
    "username",
    "walletNames",
    "walletIds"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
