package app.budgetmanager.service;

import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.mapper.ExpenseMapper;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import app.budgetmanager.model.entity.Wallet;
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
                .map(expenseMapper::toExpenseResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getAll() {
        return expenseRepository.findAll().stream().map(expenseMapper::toExpenseResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getBySenderUserId(Long senderUserId) {
        return expenseRepository.findByWalletOwnerUserId(senderUserId).stream()
                .map(expenseMapper::toExpenseResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getById(Long id) {
        return expenseRepository.findByIdWithAssociations(id)
                .map(expenseMapper::toExpenseResponseDto)
                .orElseThrow();
    }

    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto request) {
        requirePositiveAmount(request.getAmount());
        Wallet wallet = walletRepository.findById(request.getWalletId()).orElseThrow();
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
        Set<Tag> tags = resolveTags(request.getTagIds());

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.setTags(new HashSet<>(tags));

        Expense saved = expenseRepository.save(expense);
        return expenseRepository.findByIdWithAssociations(saved.getId())
                .map(expenseMapper::toExpenseResponseDto)
                .orElseThrow();
    }

    public ExpenseResponseDto createWithoutTransactional(ExpenseRequestDto dto) {
        requirePositiveAmount(dto.getAmount());
        Wallet wallet = walletRepository.findById(dto.getWalletId()).orElseThrow();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();

        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.setTags(new HashSet<>());
        Expense saved = expenseRepository.save(expense);

        Set<Tag> tags = resolveTags(dto.getTagIds());
        saved.getTags().addAll(tags);
        expenseRepository.save(saved);

        return expenseRepository.findByIdWithAssociations(saved.getId())
                .map(expenseMapper::toExpenseResponseDto)
                .orElseThrow();
    }

    @Transactional
    public ExpenseResponseDto update(Long id, ExpenseRequestDto request) {
        requirePositiveAmount(request.getAmount());
        Expense expense = expenseRepository.findByIdWithAssociations(id).orElseThrow();
        Wallet wallet = walletRepository.findById(request.getWalletId()).orElseThrow();
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
        Set<Tag> newTags = resolveTags(request.getTagIds());

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.getTags().clear();
        expense.getTags().addAll(newTags);

        expenseRepository.save(expense);
        return expenseRepository.findByIdWithAssociations(id)
                .map(expenseMapper::toExpenseResponseDto)
                .orElseThrow();
    }

    @Transactional
    public ExpenseResponseDto patch(Long id, ExpenseRequestDto request) {
        Expense expense = expenseRepository.findByIdWithAssociations(id).orElseThrow();

        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            requirePositiveAmount(request.getAmount());
            expense.setAmount(request.getAmount());
        }
        if (request.getDate() != null) {
            expense.setDate(request.getDate());
        }
        if (request.getWalletId() != null) {
            Wallet wallet = walletRepository.findById(request.getWalletId()).orElseThrow();
            expense.setWallet(wallet);
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow();
            expense.setCategory(category);
        }
        if (request.getTagIds() != null) {
            Set<Tag> newTags = resolveTags(request.getTagIds());
            expense.getTags().clear();
            expense.getTags().addAll(newTags);
        }

        expenseRepository.save(expense);
        return expenseRepository.findByIdWithAssociations(id)
                .map(expenseMapper::toExpenseResponseDto)
                .orElseThrow();
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Incorrect amount");
        }
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
