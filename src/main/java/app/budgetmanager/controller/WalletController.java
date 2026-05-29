package app.budgetmanager.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.ErrorResponseDto;
import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.service.WalletService;
import app.budgetmanager.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
@Validated
@Tag(name = "Wallets", description = "User wallet management")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Validation or bad request error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
    @ApiResponse(responseCode = "404", description = "Resource not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
    @ApiResponse(responseCode = "409", description = "Conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
    @ApiResponse(responseCode = "500", description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
})
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "List wallets, optionally filtered by user id")
    public List<WalletResponseDto> getWallets(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return walletService.getByUserId(userId);
        }
        return walletService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by id")
    public WalletResponseDto getById(@PathVariable Long id) {
        return walletService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create wallet")
    public WalletResponseDto create(
            @Validated(ValidationGroups.FullValidation.class) @RequestBody WalletRequestDto dto
    ) {
        return walletService.save(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete wallet by id")
    public void delete(@PathVariable Long id) {
        walletService.delete(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rename wallet")
    public WalletResponseDto rename(
            @PathVariable Long id,
            @Validated(ValidationGroups.RenameValidation.class) @RequestBody WalletRequestDto dto
    ) {
        return walletService.updateName(id, dto.getName());
    }
}
