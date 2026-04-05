package app.budgetmanager.service;

import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.mapper.WalletMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper mapper;

    public WalletService(
            WalletRepository walletRepository,
            UserRepository userRepository,
            WalletMapper mapper
    ) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public WalletResponseDto getById(Long id) {
        return mapper.toWalletResponseDto(walletRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDto> getByUserId(Long userId) {
        return walletRepository.findByUserId(userId).stream().map(mapper::toWalletResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDto> getAll() {
        return walletRepository.findAll().stream().map(mapper::toWalletResponseDto).toList();
    }

    @Transactional
    public WalletResponseDto save(WalletRequestDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        Wallet wallet = new Wallet();
        wallet.setName(dto.getName());
        wallet.setUser(user);
        user.getWallets().add(wallet);
        userRepository.save(user);
        return mapper.toWalletResponseDto(wallet);
    }

    public void delete(Long id) {
        walletRepository.deleteById(id);
    }

    public WalletResponseDto updateName(Long id, String name) {
        Wallet wallet = walletRepository.findById(id).orElseThrow();
        wallet.setName(name);
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    public Wallet getEntityById(Long id) {
        return walletRepository.findById(id).orElseThrow();
    }
}
