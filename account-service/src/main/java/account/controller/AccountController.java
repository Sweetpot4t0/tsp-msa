package account.controller;

import account.dto.AccountResponse;
import account.dto.AmountRequest;
import account.dto.BalanceChangeResponse;
import account.dto.CreateAccountRequest;
import account.entity.Account;
import account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/accounts")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.ownerName(), request.initialBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    @GetMapping("/api/accounts/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return AccountResponse.from(accountService.getAccount(id));
    }

    @PostMapping("/internal/accounts/{id}/debit")
    public BalanceChangeResponse debit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return new BalanceChangeResponse(accountService.debit(id, request.amount(), request.referenceId()));
    }

    @PostMapping("/internal/accounts/{id}/credit")
    public BalanceChangeResponse credit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return new BalanceChangeResponse(accountService.credit(id, request.amount(), request.referenceId()));
    }
}
