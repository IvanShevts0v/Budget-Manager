package app.budgetmanager.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.service.ExpenseService;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseResponseDto> getAll(
            @RequestParam(required = false) Long senderUserId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String category,
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

    @GetMapping("/{id}")
    public ExpenseResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ExpenseResponseDto create(@RequestBody ExpenseRequestDto dto) {
        return service.create(dto);
    }

    @PostMapping("/no-transactional")
    public ExpenseResponseDto createWithoutTransactional(@RequestBody ExpenseRequestDto dto) {
        return service.createWithoutTransactional(dto);
    }

    @PutMapping("/{id}")
    public ExpenseResponseDto update(@PathVariable Long id, @RequestBody ExpenseRequestDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
