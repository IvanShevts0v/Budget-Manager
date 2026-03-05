package app.budgetmanager.controller;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.service.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<ExpenseResponseDto> getAll(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return expenseService.findFiltered(id, description, amount, category, date);
    }

    @GetMapping("/{id}")
    public ExpenseResponseDto getById(@PathVariable Long id) {
        return expenseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));
    }
}
