package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
