package app.budgetmanager.controller;

import app.budgetmanager.dto.DemoRelatedSaveRequest;
import app.budgetmanager.service.RelatedEntitySaveDemoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo/related-save")
public class RelatedEntityDemoController {

    private static final String KEY_MODE = "mode";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ERROR = "error";
    private static final String KEY_HINT = "hint";

    private final RelatedEntitySaveDemoService demoService;

    public RelatedEntityDemoController(RelatedEntitySaveDemoService demoService) {
        this.demoService = demoService;
    }

    /**
     * Частичное сохранение: при failAfterWallet=true User и Wallet уже в БД, затем 500.
     */
    @PostMapping("/partial")
    public ResponseEntity<Map<String, Object>> partial(@Valid @RequestBody DemoRelatedSaveRequest request) {
        try {
            demoService.saveUserAndWalletPartialCommit(request);
            return ResponseEntity.ok(Map.of(
                    KEY_MODE, "partial",
                    KEY_SUCCESS, true,
                    KEY_MESSAGE, "User и Wallet сохранены"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    KEY_MODE, "partial",
                    KEY_SUCCESS, false,
                    KEY_ERROR, e.getMessage(),
                    KEY_HINT,
                    "Без @Transactional на сервисе каждый save уже закоммичен: проверьте users и wallets"
            ));
        }
    }

    /**
     * Атомарное сохранение: при failAfterWallet=true ничего не остаётся в БД.
     */
    @PostMapping("/transactional")
    public ResponseEntity<Map<String, Object>> transactional(@Valid @RequestBody DemoRelatedSaveRequest request) {
        try {
            demoService.saveUserAndWalletFullRollback(request);
            return ResponseEntity.ok(Map.of(
                    KEY_MODE, "transactional",
                    KEY_SUCCESS, true,
                    KEY_MESSAGE, "User и Wallet сохранены в одной транзакции"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    KEY_MODE, "transactional",
                    KEY_SUCCESS, false,
                    KEY_ERROR, e.getMessage(),
                    KEY_HINT, "С @Transactional на сервисе вся операция откатилась, записей в БД не должно быть"
            ));
        }
    }
}
