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
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.dto.TagDto;
import app.budgetmanager.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/tags")
@Validated
@Tag(name = "Tags", description = "Expense tag management")
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
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "List all tags")
    public List<NamedResponseDto> getAll() {
        return tagService.getAll();
    }

    @GetMapping("/by-name")
    @Operation(summary = "Get tag by name")
    public NamedResponseDto getByName(@RequestParam @NotBlank(message = "Name is required") String name) {
        return tagService.getByName(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by id")
    public NamedResponseDto getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create tag")
    public NamedResponseDto create(@Valid @RequestBody TagDto dto) {
        return tagService.save(dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update tag")
    public NamedResponseDto patch(@PathVariable Long id, @Valid @RequestBody TagDto dto) {
        return tagService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag by id")
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
