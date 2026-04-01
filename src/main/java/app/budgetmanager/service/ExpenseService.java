package app.budgetmanager.service;

import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.entity.Category;
import app.budgetmanager.entity.Expense;
import app.budgetmanager.entity.Tag;
import app.budgetmanager.entity.Wallet;
import app.budgetmanager.mapper.ExpenseMapper;
import app.budgetmanager.repository.CategoryRepository;
import app.budgetmanager.repository.ExpenseRepository;
import app.budgetmanager.repository.ExpenseSpecifications;
import app.budgetmanager.repository.TagRepository;
import app.budgetmanager.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            WalletRepository walletRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            ExpenseMapper expenseMapper
    ) {
        this.expenseRepository = expenseRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.expenseMapper = expenseMapper;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> findFiltered(
            Long id,
            String description,
            BigDecimal amount,
            String category,
            LocalDate date
    ) {
        return expenseRepository
                .findAll(ExpenseSpecifications.matchesFilter(id, description, amount, category, date))
                .stream()
                .map(expenseMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ExpenseResponseDto> findById(Long id) {
        return expenseRepository.findByIdWithAssociations(id)
                .map(expenseMapper::toDto);
    }

    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto request) {
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));
        Set<Tag> tags = resolveTags(request.tagIds());

        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.setTags(new HashSet<>(tags));

        Expense saved = expenseRepository.save(expense);
        return expenseRepository.findByIdWithAssociations(saved.getId())
                .map(expenseMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load created expense"));
    }

    @Transactional
    public ExpenseResponseDto update(Long id, ExpenseRequestDto request) {
        Expense expense = expenseRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"));
        Set<Tag> newTags = resolveTags(request.tagIds());

        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.getTags().clear();
        expense.getTags().addAll(newTags);

        expenseRepository.save(expense);
        return expenseRepository.findByIdWithAssociations(id)
                .map(expenseMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load updated expense"));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
        }
        expenseRepository.deleteById(id);
    }

    private Set<Tag> resolveTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Tag> found = tagRepository.findAllById(tagIds);
        if (found.size() != tagIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more tags not found");
        }
        return new HashSet<>(found);
    }
}
