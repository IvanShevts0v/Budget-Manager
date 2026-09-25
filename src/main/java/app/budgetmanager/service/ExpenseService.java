package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.cache.ExpenseQueryKey;
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
import app.budgetmanager.util.AtomicCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseFilterCache expenseFilterCache;
    private final AtomicCounter createdExpenseCounter;

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> findFiltered(
            Long id,
            String description,
            BigDecimal amount,
            String category,
            LocalDate date,
            Long walletOwnerUserId,
            List<String> tagNames,
            Pageable pageable
    ) {
        return expenseRepository
                .findAll(ExpenseSpecifications.matchesFilter(
                        id, description, amount, category, date, walletOwnerUserId, tagNames
                ), pageable)
                .map(expenseMapper::toExpenseResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getAll(Pageable pageable) {
        return expenseRepository.findAll(pageable).map(expenseMapper::toExpenseResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getBySenderUserId(Long senderUserId, Pageable pageable) {
        return expenseRepository.findByWalletOwnerUserId(senderUserId, pageable)
                .map(expenseMapper::toExpenseResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> findByWalletOwnerAndCategory(
            Long walletOwnerUserId,
            String categoryName,
            Pageable pageable,
            boolean useNative
    ) {
        String normalizedCategoryName = normalize(categoryName);
        ExpenseQueryKey key = new ExpenseQueryKey(walletOwnerUserId, normalizedCategoryName, pageable, useNative);
        return expenseFilterCache.getOrCompute(
                key,
                () -> (useNative
                        ? expenseRepository.findAllWithFiltersNative(
                                walletOwnerUserId, normalizedCategoryName, pageable)
                        : expenseRepository.findAllWithFiltersJpql(
                                walletOwnerUserId, normalizedCategoryName, pageable)
                ).map(expenseMapper::fromFilterView)
        );
    }

    public int getCreatedExpenseCount() {
        return createdExpenseCounter.get();
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getById(Long id) {
        return expenseMapper.toExpenseResponseDto(findExpenseWithAssociations(id));
    }

    @Transactional
    public List<ExpenseResponseDto> createBulk(List<ExpenseRequestDto> requests) {
        return requests.stream()
                .map(this::create)
                .toList();
    }

    public List<ExpenseResponseDto> createBulkWithoutTransactional(List<ExpenseRequestDto> requests) {
        return requests.stream()
                .map(this::createWithoutTransactional)
                .toList();
    }

    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto request) {
        requirePositiveAmount(request.getAmount());
        Wallet wallet = findWalletById(request.getWalletId());
        Category category = findCategoryById(request.getCategoryId());
        Set<Tag> tags = resolveTags(request.getTagIds());

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.setTags(new HashSet<>(tags));

        Expense saved = expenseRepository.save(expense);
        createdExpenseCounter.increment();
        expenseFilterCache.invalidate();
        return expenseMapper.toExpenseResponseDto(findExpenseWithAssociations(saved.getId()));
    }

    public ExpenseResponseDto createWithoutTransactional(ExpenseRequestDto dto) {
        requirePositiveAmount(dto.getAmount());
        Wallet wallet = findWalletById(dto.getWalletId());
        Category category = findCategoryById(dto.getCategoryId());
        Set<Tag> tags = resolveTags(dto.getTagIds());

        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.setTags(new HashSet<>(tags));
        Expense saved = expenseRepository.save(expense);
        createdExpenseCounter.increment();
        expenseFilterCache.invalidate();

        return expenseMapper.toExpenseResponseDto(findExpenseWithAssociations(saved.getId()));
    }

    @Transactional
    public ExpenseResponseDto update(Long id, ExpenseRequestDto request) {
        requirePositiveAmount(request.getAmount());
        Expense expense = findExpenseWithAssociations(id);
        Wallet wallet = findWalletById(request.getWalletId());
        Category category = findCategoryById(request.getCategoryId());
        Set<Tag> newTags = resolveTags(request.getTagIds());

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setWallet(wallet);
        expense.setCategory(category);
        expense.getTags().clear();
        expense.getTags().addAll(newTags);

        expenseRepository.save(expense);
        expenseFilterCache.invalidate();
        return expenseMapper.toExpenseResponseDto(findExpenseWithAssociations(id));
    }

    @Transactional
    public ExpenseResponseDto patch(Long id, ExpenseRequestDto request) {
        Expense expense = findExpenseWithAssociations(id);

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
            Wallet wallet = findWalletById(request.getWalletId());
            expense.setWallet(wallet);
        }
        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            expense.setCategory(category);
        }
        if (request.getTagIds() != null) {
            Set<Tag> newTags = resolveTags(request.getTagIds());
            expense.getTags().clear();
            expense.getTags().addAll(newTags);
        }

        expenseRepository.save(expense);
        expenseFilterCache.invalidate();
        return expenseMapper.toExpenseResponseDto(findExpenseWithAssociations(id));
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.deleteById(id);
        expenseFilterCache.invalidate();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Incorrect amount");
        }
    }

    private Set<Tag> resolveTags(List<Long> tagIds) {
        return Optional.ofNullable(tagIds)
                .filter(ids -> !ids.isEmpty())
                .map(ids -> {
                    List<Tag> found = tagRepository.findAllById(ids);
                    if (found.size() != ids.size()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more tags not found");
                    }
                    return found.stream().collect(Collectors.toCollection(HashSet::new));
                })
                .orElseGet(HashSet::new);
    }

    private Wallet findWalletById(Long walletId) {
        return Optional.ofNullable(walletId)
                .flatMap(walletRepository::findById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }

    private Category findCategoryById(Long categoryId) {
        return Optional.ofNullable(categoryId)
                .flatMap(categoryRepository::findById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private Expense findExpenseWithAssociations(Long expenseId) {
        return Optional.ofNullable(expenseId)
                .flatMap(expenseRepository::findByIdWithAssociations)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }
}
