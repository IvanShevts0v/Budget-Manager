package app.budgetmanager.controller;

import java.util.List;

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

import app.budgetmanager.dto.CategoryRequestDto;
import app.budgetmanager.dto.ErrorResponseDto;
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/categories")
@Validated
@Tag(name = "Categories", description = "Expense category management")
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
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List all categories")
    public List<NamedResponseDto> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/by-name/exact")
    @Operation(summary = "Get category by exact name")
    public NamedResponseDto getByNameExact(@RequestParam @NotBlank(message = "Name is required") String name) {
        return categoryService.getByNameExact(name);
    }

    @GetMapping("/by-name")
    @Operation(summary = "Search categories by name")
    public List<NamedResponseDto> getByName(@RequestParam @NotBlank(message = "Name is required") String name) {
        return categoryService.getByName(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public NamedResponseDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create category")
    public NamedResponseDto create(@Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.create(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category by id")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace category")
    public NamedResponseDto putCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update category")
    public NamedResponseDto patchCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.patch(id, dto);
    }
}
