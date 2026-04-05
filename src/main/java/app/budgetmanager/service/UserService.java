package app.budgetmanager.service;

import app.budgetmanager.dto.UserRequestDto;
import app.budgetmanager.dto.UserResponseDto;
import app.budgetmanager.mapper.UserMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import app.budgetmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return mapper.toUserResponseDto(repository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return repository.findAll().stream().map(mapper::toUserResponseDto).toList();
    }

    @Transactional
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        String username = userRequestDto.getUsername() == null ? "" : userRequestDto.getUsername().trim();
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
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

    public void delete(Long id) {
        repository.deleteById(id);
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
