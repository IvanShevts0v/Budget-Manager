package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.mapper.WalletMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletMapper mapper;

    private WalletService service;

    @BeforeEach
    void setUp() {
        service = new WalletService(walletRepository, userRepository, mapper, new ExpenseFilterCache());
    }

    @Test // чтение по идентификатору
    void getByIdShouldMapWallet() {
        Wallet wallet = wallet(1L);
        WalletResponseDto response = new WalletResponseDto(1L, "Main", 2L, "ivan");
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(mapper.toWalletResponseDto(wallet)).thenReturn(response);

        assertEquals(response, service.getById(1L));
    }

    @Test // чтение отсутствующей записи
    void getByIdShouldThrowWhenMissing() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(1L));
    }

    @Test // список по владельцу
    void getByUserIdShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Wallet wallet = wallet(1L);
        when(walletRepository.findByUserId(2L, pageable)).thenReturn(new PageImpl<>(java.util.List.of(wallet)));
        when(mapper.toWalletResponseDto(wallet)).thenReturn(new WalletResponseDto(1L, "Main", 2L, "ivan"));

        Page<WalletResponseDto> result = service.getByUserId(2L, pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test // полный список
    void getAllShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Wallet wallet = wallet(1L);
        when(walletRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(wallet)));
        when(mapper.toWalletResponseDto(wallet)).thenReturn(new WalletResponseDto(1L, "Main", 2L, "ivan"));

        assertEquals(1, service.getAll(pageable).getTotalElements());
    }

    @Test // создание и привязка к владельцу
    void saveShouldAttachWalletToUser() {
        User user = new User();
        user.setId(2L);
        WalletRequestDto request = new WalletRequestDto(2L, "Карта");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.toWalletResponseDto(any(Wallet.class)))
                .thenReturn(new WalletResponseDto(null, "Карта", 2L, null));

        WalletResponseDto result = service.save(request);

        assertEquals("Карта", result.getName());
        assertEquals(1, user.getWallets().size());
        verify(userRepository).save(user);
    }

    @Test // создание при отсутствии владельца
    void saveShouldThrowWhenUserMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        WalletRequestDto request = new WalletRequestDto(2L, "Карта");
        assertThrows(NoSuchElementException.class, () -> service.save(request));
    }

    @Test // удаление
    void deleteShouldRemoveWallet() {
        service.delete(1L);

        verify(walletRepository).deleteById(1L);
    }

    @Test // переименование
    void updateNameShouldPersistNewName() {
        Wallet wallet = wallet(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);
        when(mapper.toWalletResponseDto(wallet)).thenReturn(new WalletResponseDto(1L, "New", 2L, "ivan"));

        WalletResponseDto result = service.updateName(1L, "New");

        assertEquals("New", result.getName());
        assertEquals("New", wallet.getName());
    }

    @Test // получение сущности
    void getEntityByIdShouldReturnWallet() {
        Wallet wallet = wallet(1L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));

        assertEquals(wallet, service.getEntityById(1L));
    }

    @Test // получение отсутствующей сущности
    void getEntityByIdShouldThrowWhenMissing() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getEntityById(1L));
    }

    @Test // переименование отсутствующей записи
    void updateNameShouldThrowWhenMissing() {
        when(walletRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.updateName(1L, "New"));
    }

    private static Wallet wallet(Long id) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setName("Main");
        return wallet;
    }
}
