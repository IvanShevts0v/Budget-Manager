package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.ExpenseFilterView;
import app.budgetmanager.dto.ExpenseSearchCriteria;
import app.budgetmanager.dto.ExpenseRequestDto;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.mapper.ExpenseMapper;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import static org.mockito.Mockito.mock;
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

    @Test // успешное создание и ответ
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

    @Test // зависимость не найдена — отказ без записи
    void createShouldThrowWhenWalletNotFound() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");

        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.create(request));

        assertEquals(0, createdExpenseCounter.get());
        verify(walletRepository).findById(WALLET_ID);
        verify(expenseRepository, never()).save(any());
        verifyNoMoreInteractions(categoryRepository, tagRepository, expenseMapper);
    }

    @Test // пакетное создание всех элементов
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

    @Test // ошибка в пакете останавливает остальные
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

    @Test // пакет без общей транзакции
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

    @Test // нарушение инварианта — отказ без записи
    void createShouldThrowWhenAmountIsNotPositive() {
        ExpenseRequestDto request = expenseRequest("Coffee", "0");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> expenseService.create(request));

        assertEquals("Incorrect amount", exception.getMessage());
        verify(expenseRepository, never()).save(any());
    }

    @Test // зависимость категории не найдена
    void createShouldThrowWhenCategoryNotFound() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet(WALLET_ID)));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.create(request));
        verify(expenseRepository, never()).save(any());
    }

    @Test // часть связанных меток не найдена
    void createShouldThrowWhenTagsMissing() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        request.setTagIds(List.of(1L, 2L));
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet(WALLET_ID)));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category(CATEGORY_ID, "Food")));
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(new Tag(1L, "важно")));

        assertThrows(ResponseStatusException.class, () -> expenseService.create(request));
        verify(expenseRepository, never()).save(any());
    }

    @Test // создание с найденными связанными метками
    void createShouldSaveWhenTagsResolved() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        request.setTagIds(List.of(1L));
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense saved = expense(EXPENSE_ID, "Coffee", wallet, category);
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(new Tag(1L, "важно")));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(saved));
        when(expenseMapper.toExpenseResponseDto(saved)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        ExpenseResponseDto result = expenseService.create(request);

        assertEquals(EXPENSE_ID, result.getId());
        verify(tagRepository).findAllById(List.of(1L));
    }

    @Test // создание вне общей транзакции
    void createWithoutTransactionalShouldSaveAndReturnExpense() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense saved = expense(EXPENSE_ID, "Coffee", wallet, category);
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(saved));
        when(expenseMapper.toExpenseResponseDto(saved)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        ExpenseResponseDto result = expenseService.createWithoutTransactional(request);

        assertEquals(EXPENSE_ID, result.getId());
        assertEquals(1, createdExpenseCounter.get());
    }

    @Test // чтение по идентификатору
    void getByIdShouldReturnMappedExpense() {
        Expense saved = expense(EXPENSE_ID, "Coffee", wallet(WALLET_ID), category(CATEGORY_ID, "Food"));
        ExpenseResponseDto response = expenseResponse(EXPENSE_ID, "Coffee");
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(saved));
        when(expenseMapper.toExpenseResponseDto(saved)).thenReturn(response);

        assertEquals(response, expenseService.getById(EXPENSE_ID));
    }

    @Test // чтение отсутствующей записи
    void getByIdShouldThrowWhenMissing() {
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.getById(EXPENSE_ID));
    }

    @Test // список без фильтра
    void getAllShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Expense entity = expense(EXPENSE_ID, "Coffee", wallet(WALLET_ID), category(CATEGORY_ID, "Food"));
        ExpenseResponseDto response = expenseResponse(EXPENSE_ID, "Coffee");
        when(expenseRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(expenseMapper.toExpenseResponseDto(entity)).thenReturn(response);

        Page<ExpenseResponseDto> result = expenseService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(response, result.getContent().get(0));
    }

    @Test // список по владельцу кошелька
    void getBySenderUserIdShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Expense entity = expense(EXPENSE_ID, "Coffee", wallet(WALLET_ID), category(CATEGORY_ID, "Food"));
        when(expenseRepository.findByWalletOwnerUserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(expenseMapper.toExpenseResponseDto(entity)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        assertEquals(1, expenseService.getBySenderUserId(1L, pageable).getContent().size());
    }

    @Test // список по спецификации фильтра
    void findFilteredShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Expense entity = expense(EXPENSE_ID, "Coffee", wallet(WALLET_ID), category(CATEGORY_ID, "Food"));
        when(expenseRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Expense>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(expenseMapper.toExpenseResponseDto(entity)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        Page<ExpenseResponseDto> result = expenseService.findFiltered(
                new ExpenseSearchCriteria(
                        EXPENSE_ID, "Coffee", new BigDecimal("5.00"), "Food", LocalDate.of(2026, 5, 27), 1L, null
                ),
                pageable
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test // сложный фильтр JPQL и повтор из кэша
    void findByWalletOwnerAndCategoryShouldUseJpqlAndCache() {
        Pageable pageable = PageRequest.of(0, 10);
        ExpenseFilterView row = mock(ExpenseFilterView.class);
        ExpenseResponseDto response = expenseResponse(EXPENSE_ID, "Coffee");
        when(expenseRepository.findAllWithFiltersJpql(1L, "еда", pageable))
                .thenReturn(new PageImpl<>(List.of(row)));
        when(expenseMapper.fromFilterView(row)).thenReturn(response);

        Page<ExpenseResponseDto> first = expenseService.findByWalletOwnerAndCategory(1L, " еда ", pageable, false);
        Page<ExpenseResponseDto> second = expenseService.findByWalletOwnerAndCategory(1L, "еда", pageable, false);

        assertEquals(response, first.getContent().get(0));
        assertEquals(first, second);
        verify(expenseRepository).findAllWithFiltersJpql(1L, "еда", pageable);
    }

    @Test // сложный фильтр native
    void findByWalletOwnerAndCategoryShouldUseNativeQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        ExpenseFilterView row = mock(ExpenseFilterView.class);
        when(expenseRepository.findAllWithFiltersNative(1L, null, pageable))
                .thenReturn(new PageImpl<>(List.of(row)));
        when(expenseMapper.fromFilterView(row)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        Page<ExpenseResponseDto> result = expenseService.findByWalletOwnerAndCategory(1L, "  ", pageable, true);

        assertEquals(1, result.getContent().size());
        verify(expenseRepository).findAllWithFiltersNative(1L, null, pageable);
    }

    @Test // чтение счётчика созданных записей
    void getCreatedExpenseCountShouldReturnCounterValue() {
        assertEquals(0, expenseService.getCreatedExpenseCount());
    }

    @Test // полная замена полей
    void updateShouldReplaceFields() {
        ExpenseRequestDto request = expenseRequest("Lunch", "12.50");
        request.setTagIds(List.of());
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense existing = expense(EXPENSE_ID, "Coffee", wallet, category);
        ExpenseResponseDto response = expenseResponse(EXPENSE_ID, "Lunch");
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(existing));
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(expenseRepository.save(existing)).thenReturn(existing);
        when(expenseMapper.toExpenseResponseDto(existing)).thenReturn(response);

        assertEquals(response, expenseService.update(EXPENSE_ID, request));
        verify(expenseRepository).save(existing);
    }

    @Test // замена при некорректной сумме
    void updateShouldThrowWhenAmountInvalid() {
        ExpenseRequestDto request = expenseRequest("Lunch", "0");

        assertThrows(IllegalArgumentException.class, () -> expenseService.update(EXPENSE_ID, request));
        verify(expenseRepository, never()).save(any());
    }

    @Test // частичное обновление переданных полей
    void patchShouldUpdateProvidedFields() {
        ExpenseRequestDto request = new ExpenseRequestDto();
        request.setDescription("Tea");
        request.setAmount(new BigDecimal("3.00"));
        request.setDate(LocalDate.of(2026, 6, 1));
        request.setWalletId(WALLET_ID);
        request.setCategoryId(CATEGORY_ID);
        request.setTagIds(List.of(1L));
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense existing = expense(EXPENSE_ID, "Coffee", wallet, category);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(existing));
        when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(new Tag(1L, "важно")));
        when(expenseRepository.save(existing)).thenReturn(existing);
        when(expenseMapper.toExpenseResponseDto(existing)).thenReturn(expenseResponse(EXPENSE_ID, "Tea"));

        ExpenseResponseDto result = expenseService.patch(EXPENSE_ID, request);

        assertEquals("Tea", result.getDescription());
        assertEquals("Tea", existing.getDescription());
        verify(expenseRepository).save(existing);
    }

    @Test // частичное обновление при некорректной сумме
    void patchShouldThrowWhenAmountInvalid() {
        ExpenseRequestDto request = new ExpenseRequestDto();
        request.setAmount(new BigDecimal("-1"));
        Expense existing = expense(EXPENSE_ID, "Coffee", wallet(WALLET_ID), category(CATEGORY_ID, "Food"));
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> expenseService.patch(EXPENSE_ID, request));
    }

    @Test // удаление и сброс кэша
    void deleteShouldRemoveRecord() {
        expenseService.delete(EXPENSE_ID);

        verify(expenseRepository).deleteById(EXPENSE_ID);
    }

    @Test // отказ при пустой сумме
    void createShouldThrowWhenAmountMissing() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        request.setAmount(null);

        assertThrows(IllegalArgumentException.class, () -> expenseService.create(request));
        verify(expenseRepository, never()).save(any());
    }

    @Test // отказ при пустом идентификаторе кошелька
    void createShouldThrowWhenWalletIdMissing() {
        ExpenseRequestDto request = expenseRequest("Coffee", "5.00");
        request.setWalletId(null);

        assertThrows(ResponseStatusException.class, () -> expenseService.create(request));
        verify(walletRepository, never()).findById(anyLong());
    }

    @Test // замена отсутствующей записи
    void updateShouldThrowWhenExpenseMissing() {
        ExpenseRequestDto request = expenseRequest("Lunch", "12.50");
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> expenseService.update(EXPENSE_ID, request));
        verify(expenseRepository, never()).save(any());
    }

    @Test // частичное обновление без переданных полей
    void patchShouldKeepFieldsWhenRequestEmpty() {
        ExpenseRequestDto request = new ExpenseRequestDto();
        Wallet wallet = wallet(WALLET_ID);
        Category category = category(CATEGORY_ID, "Food");
        Expense existing = expense(EXPENSE_ID, "Coffee", wallet, category);
        when(expenseRepository.findByIdWithAssociations(EXPENSE_ID)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(existing)).thenReturn(existing);
        when(expenseMapper.toExpenseResponseDto(existing)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        ExpenseResponseDto result = expenseService.patch(EXPENSE_ID, request);

        assertEquals("Coffee", result.getDescription());
        assertEquals("Coffee", existing.getDescription());
    }

    @Test // сложный фильтр без имени категории
    void findByWalletOwnerAndCategoryShouldTreatNullCategoryAsUnfiltered() {
        Pageable pageable = PageRequest.of(0, 10);
        ExpenseFilterView row = mock(ExpenseFilterView.class);
        when(expenseRepository.findAllWithFiltersJpql(1L, null, pageable))
                .thenReturn(new PageImpl<>(List.of(row)));
        when(expenseMapper.fromFilterView(row)).thenReturn(expenseResponse(EXPENSE_ID, "Coffee"));

        Page<ExpenseResponseDto> result = expenseService.findByWalletOwnerAndCategory(1L, null, pageable, false);

        assertEquals(1, result.getContent().size());
        verify(expenseRepository).findAllWithFiltersJpql(1L, null, pageable);
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
