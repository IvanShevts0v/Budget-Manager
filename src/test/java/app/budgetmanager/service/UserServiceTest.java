package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.UserRequestDto;
import app.budgetmanager.dto.UserResponseDto;
import app.budgetmanager.mapper.UserMapper;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    private ExpenseFilterCache expenseFilterCache;
    private UserService service;

    @BeforeEach
    void setUp() {
        expenseFilterCache = new ExpenseFilterCache();
        service = new UserService(repository, mapper, expenseFilterCache);
    }

    @Test // чтение по идентификатору
    void getByIdShouldMapUser() {
        User user = user(1L, "ivan");
        UserResponseDto response = new UserResponseDto(1L, "ivan", List.of(), List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toUserResponseDto(user)).thenReturn(response);

        assertEquals(response, service.getById(1L));
    }

    @Test // чтение отсутствующей записи
    void getByIdShouldThrowWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(1L));
    }

    @Test // постраничный список
    void getAllShouldMapPage() {
        User user = user(1L, "ivan");
        UserResponseDto response = new UserResponseDto(1L, "ivan", List.of(), List.of());
        when(repository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(user)));
        when(mapper.toUserResponseDto(user)).thenReturn(response);

        Page<UserResponseDto> result = service.getAll(PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals(response, result.getContent().get(0));
    }

    @Test // регистрация с именем кошелька по умолчанию
    void registerUserShouldCreateDefaultWallet() {
        UserRequestDto request = new UserRequestDto("ivan", "  ");
        when(repository.existsByUsername("ivan")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(mapper.toUserResponseDto(any(User.class)))
                .thenReturn(new UserResponseDto(1L, "ivan", List.of("Default"), List.of()));

        UserResponseDto result = service.registerUser(request);

        assertEquals("ivan", result.getUsername());
        verify(repository).save(any(User.class));
    }

    @Test // регистрация с заданным именем кошелька
    void registerUserShouldUseProvidedWalletName() {
        UserRequestDto request = new UserRequestDto("ivan", " Основной ");
        when(repository.existsByUsername("ivan")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toUserResponseDto(any(User.class)))
                .thenReturn(new UserResponseDto(1L, "ivan", List.of("Основной"), List.of()));

        service.registerUser(request);

        verify(repository).save(any(User.class));
    }

    @Test // конфликт уникального имени
    void registerUserShouldThrowWhenUsernameExists() {
        when(repository.existsByUsername("ivan")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.registerUser(new UserRequestDto("ivan", null)));
        verify(repository, never()).save(any());
    }

    @Test // удаление вместе со связанными сущностями
    void deleteShouldClearAssociationsAndRemoveUser() {
        User user = user(1L, "ivan");
        Wallet wallet = new Wallet();
        wallet.setId(2L);
        wallet.setUser(user);
        wallet.getExpenses().add(new Expense());
        user.getWallets().add(wallet);
        when(repository.findByIdWithWallets(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        assertEquals(0, user.getWallets().size());
        verify(repository).delete(user);
    }

    @Test // удаление отсутствующей записи
    void deleteShouldThrowWhenMissing() {
        when(repository.findByIdWithWallets(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(1L));
    }

    @Test // смена имени при отсутствии конфликта
    void patchShouldUpdateUsername() {
        User user = user(1L, "ivan");
        UserResponseDto response = new UserResponseDto(1L, "alex", List.of(), List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByUsername("alex")).thenReturn(false);
        when(repository.save(user)).thenReturn(user);
        when(mapper.toUserResponseDto(user)).thenReturn(response);

        assertEquals(response, service.patch(1L, new UserRequestDto("alex", null)));
        assertEquals("alex", user.getUsername());
    }

    @Test // то же имя не проверяется на конфликт
    void patchShouldKeepSameUsername() {
        User user = user(1L, "ivan");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toUserResponseDto(user)).thenReturn(new UserResponseDto(1L, "ivan", List.of(), List.of()));

        service.patch(1L, new UserRequestDto("ivan", null));

        verify(repository, never()).existsByUsername("ivan");
    }

    @Test // конфликт при смене имени
    void patchShouldThrowWhenUsernameTaken() {
        User user = user(1L, "ivan");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByUsername("alex")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.patch(1L, new UserRequestDto("alex", null)));
        verify(repository, never()).save(any());
    }

    @Test // частичное обновление отсутствующей записи
    void patchShouldThrowWhenUserMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.patch(1L, new UserRequestDto("alex", null)));
        verify(repository, never()).save(any());
    }

    @Test // частичное обновление без смены имени
    void patchShouldSkipUsernameWhenNull() {
        User user = user(1L, "ivan");
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toUserResponseDto(user)).thenReturn(new UserResponseDto(1L, "ivan", List.of(), List.of()));

        service.patch(1L, new UserRequestDto());

        assertEquals("ivan", user.getUsername());
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
