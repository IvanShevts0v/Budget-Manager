package app.budgetmanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.service.WalletService;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public List<WalletResponseDto> getWallets(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return walletService.getByUserId(userId);
        }
        return walletService.getAll();
    }

    @GetMapping("/{id}")
    public WalletResponseDto getById(@PathVariable Long id) {
        return walletService.getById(id);
    }

    @PostMapping
    public WalletResponseDto create(@RequestBody WalletRequestDto dto) {
        return walletService.save(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        walletService.delete(id);
    }

    @PatchMapping("/{id}")
    public WalletResponseDto rename(@PathVariable Long id, @PathVariable String name) {
        return walletService.updateName(id, name);
    }
}
