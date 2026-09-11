package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.mapper.WalletMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper mapper;
    private final ExpenseFilterCache expenseFilterCache;


    @Transactional(readOnly = true)
    public WalletResponseDto getById(Long id) {
        return mapper.toWalletResponseDto(walletRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<WalletResponseDto> getByUserId(Long userId, Pageable pageable) {
        return walletRepository.findByUserId(userId, pageable).map(mapper::toWalletResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<WalletResponseDto> getAll(Pageable pageable) {
        return walletRepository.findAll(pageable).map(mapper::toWalletResponseDto);
    }

    @Transactional
    public WalletResponseDto save(WalletRequestDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        Wallet wallet = new Wallet();
        wallet.setName(dto.getName());
        wallet.setUser(user);
        user.getWallets().add(wallet);
        userRepository.save(user);
        expenseFilterCache.invalidate();
        return mapper.toWalletResponseDto(wallet);
    }

    public void delete(Long id) {
        walletRepository.deleteById(id);
        expenseFilterCache.invalidate();
    }

    public WalletResponseDto updateName(Long id, String name) {
        Wallet wallet = walletRepository.findById(id).orElseThrow();
        wallet.setName(name);
        WalletResponseDto response = mapper.toWalletResponseDto(walletRepository.save(wallet));
        expenseFilterCache.invalidate();
        return response;
    }

    public Wallet getEntityById(Long id) {
        return walletRepository.findById(id).orElseThrow();
    }
}
