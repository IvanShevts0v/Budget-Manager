package app.budgetmanager.service;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.mapper.ExpenseMapper;
import app.budgetmanager.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    public List<ExpenseResponseDto> findFiltered(
            Long id,
            String description,
            BigDecimal amount,
            String category,
            LocalDate date
    ) {
        return expenseRepository.findAll().stream()
                .filter(expense -> id == null || expense.getId().equals(id))
                .filter(expense -> description == null || description.isEmpty()
                        || expense.getDescription().equalsIgnoreCase(description))
                .filter(expense -> amount == null || expense.getAmount().compareTo(amount) == 0)
                .filter(expense -> category == null || category.isEmpty()
                        || expense.getCategory().equalsIgnoreCase(category))
                .filter(expense -> date == null || expense.getDate().equals(date))
                .map(expenseMapper::toDto)
                .toList();
    }

    public Optional<ExpenseResponseDto> findById(Long id) {
        return expenseRepository.findById(id)
                .map(expenseMapper::toDto);
    }
}


