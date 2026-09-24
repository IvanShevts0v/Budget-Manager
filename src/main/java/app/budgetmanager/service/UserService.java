package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.UserRequestDto;
import app.budgetmanager.dto.UserResponseDto;
import app.budgetmanager.mapper.UserMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final ExpenseFilterCache expenseFilterCache;

    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return mapper.toUserResponseDto(repository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toUserResponseDto);
    }

    @Transactional
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        String username = userRequestDto.getUsername().trim();
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        Wallet wallet = new Wallet();
        String walletName = userRequestDto.getDefaultWalletName();
        wallet.setName(walletName != null && !walletName.isBlank() ? walletName.trim() : "Default");
        wallet.setUser(user);
        user.getWallets().add(wallet);
        User savedUser = repository.save(user);
        return mapper.toUserResponseDto(savedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = repository.findByIdWithWallets(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.getWallets().forEach(wallet -> wallet.getExpenses().clear());
        user.getWallets().clear();
        repository.delete(user);
        expenseFilterCache.invalidate();
    }

    @Transactional
    public UserResponseDto patch(Long id, UserRequestDto dto) {
        User user = repository.findById(id).orElseThrow();
        if (dto.getUsername() != null) {
            String trimmed = dto.getUsername().trim();
            if (!trimmed.equals(user.getUsername()) && repository.existsByUsername(trimmed)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(trimmed);
        }
        return mapper.toUserResponseDto(repository.save(user));
    }
}
