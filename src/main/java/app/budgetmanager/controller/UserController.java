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
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.ErrorResponseDto;
import app.budgetmanager.dto.UserRequestDto;
import app.budgetmanager.dto.UserResponseDto;
import app.budgetmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
@Tag(name = "Users", description = "User registration and management")
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
public class UserController {

    private final UserService service;

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserResponseDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public UserResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user with a default wallet")
    public UserResponseDto register(@Valid @RequestBody UserRequestDto userRequestDto) {
        return service.registerUser(userRequestDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user information")
    public UserResponseDto changeUserInformation(@PathVariable Long id, @Valid @RequestBody UserRequestDto dto) {
        return service.patch(id, dto);
    }
}
