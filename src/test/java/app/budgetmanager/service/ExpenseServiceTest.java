package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.mapper.ExpenseMapper;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.CategoryRepository;
import app.budgetmanager.repository.ExpenseRepository;
import app.budgetmanager.repository.TagRepository;
import app.budgetmanager.repository.WalletRepository;
import app.budgetmanager.util.AtomicCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    private static final Long WALLET_ID = 1L;
    private static final Long CATEGORY_ID = 2L;
    private static final Long EXPENSE_ID = 10L;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    private ExpenseFilterCache expenseFilterCache;
    private AtomicCounter createdExpenseCounter;
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseFilterCache = new ExpenseFilterCache();
        createdExpenseCounter = new AtomicCounter();
        expenseService = new ExpenseService(
                expenseRepository,
                walletRepository,
                categoryRepository,
                tagRepository,
                expenseMapper,
                expenseFilterCache,
                createdExpenseCounter
        );
    }

    @Test
    void createShouldSaveAndReturnExpense() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense saved = expense(EXPENSE_ID, "Coffee", wallet, category);
        ExpenseResponseDto response = expenseResponse(EXPENSE_ID, "Coffee");

        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(saved));
        when(expenseMapper.toExpenseResponseDto(saved)).thenReturn(response);

        ExpenseResponseDto result = expenseService.create(request);

        assertEquals(response, result);
        assertEquals(1, createdExpenseCounter.get());
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseRepository).findByIdWithAssociations(EXPENSE_ID);
        verify(expenseMapper).toExpenseResponseDto(saved);
    }

    @Test
    void createShouldThrowWhenWalletNotFound() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");

        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.create(request));

        assertEquals(0, createdExpenseCounter.get());
        verify(walletRepository).findById(WALLET_ID);
        verify(expenseRepository, never()).save(any());
        verifyNoMoreInteractions(categoryRepository, tagRepository, expenseMapper);
    }

    @Test
    void createBulkShouldCreateEachExpense() {
        ExpenseRequestDto first = expenseRequest("Coffee", "5.00");
        ExpenseRequestDto second = expenseRequest("Lunch", "12.50");
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        List<Expense> savedExpenses = new ArrayList<>();

        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(EXPENSE_ID + savedExpenses.size());
            savedExpenses.add(expense);
            return expense;
        });
        when(expenseRepository.findByIdWithAssociations(anyLong())).thenAnswer(invocation -> savedExpenses.stream()
                .filter(expense -> expense.getId().equals(invocation.getArgument(0)))
                .findFirst());
        when(expenseMapper.toExpenseResponseDto(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            return expenseResponse(expense.getId(), expense.getDescription());
        });

        List<ExpenseResponseDto> result = expenseService.createBulk(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals("Coffee", result.get(0).getDescription());
        assertEquals("Lunch", result.get(1).getDescription());
        assertEquals(2, createdExpenseCounter.get());
        verify(expenseRepository, times(2)).save(any(Expense.class));
    }

    @Test
    void createBulkShouldStopOnFirstFailureAndNotSaveSecond() {
        ExpenseRequestDto valid = expenseRequest("Coffee", "5.00");
        ExpenseRequestDto invalid = expenseRequest("Lunch", "12.50");
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");

        when(walletRepository.findById(WALLET_ID))
                .thenReturn(Optional.of(wallet))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(EXPENSE_ID);
            return expense;
        });
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(
                expense(EXPENSE_ID, "Coffee", wallet, category)
        ));
        when(expenseMapper.toExpenseResponseDto(any(Expense.class)))
                .thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        List<ExpenseRequestDto> requests = List.of(valid, invalid);
        assertThrows(ResponseStatusException.class, () -> expenseService.createBulk(requests));

        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createBulkWithoutTransactionalShouldDelegateToNonTransactionalCreate() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense saved = expense(EXPENSE_ID, "Coffee", wallet, category);

        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(saved));
        when(expenseMapper.toExpenseResponseDto(saved)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        List<ExpenseResponseDto> result = expenseService.createBulkWithoutTransactional(List.of(request));

        assertEquals(1, result.size());
        assertEquals(1, createdExpenseCounter.get());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createShouldThrowWhenAmountIsNotPositive() {
        ExpenseRequestDto request = expenseRequest("Coffee", "0");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> expenseService.create(request));

        assertEquals("Incorrect amount", exception.getMessage());
        verify(expenseRepository, never()).save(any());
    }

    private static ExpenseRequestDto expenseRequest(String description, String amount) {
        ExpenseRequestDto dto = new ExpenseRequestDto();
        dto.setDescription(description);
        dto.setAmount(new BigDecimal(amount));
        dto.setDate(LocalDate.of(2026, 5, 27));
        dto.setWalletId(WALLET_ID);
        dto.setCategoryId(CATEGORY_ID);
        return dto;
    }

    private static ExpenseResponseDto expenseResponse(Long id, String description) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(id);
        dto.setDescription(description);
        return dto;
    }

    private static Wallet wallet(Long id) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setName("Main");
        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        wallet.setUser(user);
        return wallet;
    }

    private static Category category(Long id, String name) {
        return new Category(id, name);
    }

    private static Expense expense(Long id, String description, Wallet wallet, Category category) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setDescription(description);
        expense.setAmount(new BigDecimal("5.00"));
        expense.setDate(LocalDate.of(2026, 5, 27));
        expense.setWallet(wallet);
        expense.setCategory(category);
        return expense;
    }
}
