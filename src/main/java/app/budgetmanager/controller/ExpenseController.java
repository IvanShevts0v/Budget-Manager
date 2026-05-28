package app.budgetmanager.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.ErrorResponseDto;
import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.service.ExpenseService;
import app.budgetmanager.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/expenses")
@Validated
@Tag(name = "Expenses", description = "Expense tracking and filtering")
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
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List or filter expenses")
    public List<ExpenseResponseDto> getAll(
            @Parameter(description = "Filter by wallet owner user id")
            @RequestParam(required = false) Long senderUserId,
            @Parameter(description = "Filter by expense id")
            @RequestParam(required = false) Long id,
            @Parameter(description = "Filter by description")
            @RequestParam(required = false) String description,
            @Parameter(description = "Filter by amount")
            @RequestParam(required = false) BigDecimal amount,
            @Parameter(description = "Filter by category name")
            @RequestParam(required = false) String category,
            @Parameter(description = "Filter by date (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (senderUserId != null) {
            return service.getBySenderUserId(senderUserId);
        }
        if (id != null || (description != null && !description.isEmpty()) || amount != null
                || (category != null && !category.isEmpty()) || date != null) {
            return service.findFiltered(id, description, amount, category, date);
        }
        return service.getAll();
    }

    @GetMapping("/by-wallet-and-category")
    @Operation(summary = "Filter expenses by wallet owner and category with pagination")
    public Page<ExpenseResponseDto> findByWalletAndCategory(
            @Parameter(description = "Wallet owner user id")
            @RequestParam(required = false) Long walletOwnerUserId,
            @Parameter(description = "Category name")
            @RequestParam(required = false) String categoryName,
            @Parameter(description = "Use native SQL when true")
            @RequestParam(name = "native", defaultValue = "false") String useNative,
            Pageable pageable
    ) {
        return service.findByWalletOwnerAndCategory(
                walletOwnerUserId,
                categoryName,
                pageable,
                "true".equalsIgnoreCase(useNative)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by id")
    public ExpenseResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create expense")
    public ExpenseResponseDto create(
            @Validated(ValidationGroups.FullValidation.class) @RequestBody ExpenseRequestDto dto
    ) {
        return service.create(dto);
    }

    @PostMapping("/no-transactional")
    @Operation(summary = "Create expense without transactional wrapper")
    public ExpenseResponseDto createWithoutTransactional(
            @Validated(ValidationGroups.FullValidation.class) @RequestBody ExpenseRequestDto dto
    ) {
        return service.createWithoutTransactional(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace expense")
    public ExpenseResponseDto update(
            @PathVariable Long id,
            @Validated(ValidationGroups.FullValidation.class) @RequestBody ExpenseRequestDto dto
    ) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update expense")
    public ExpenseResponseDto patch(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDto dto) {
        return service.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense by id")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
