package account.service;

import account.entity.Account;
import account.exception.AccountNotFoundException;
import account.exception.InsufficientBalanceException;
import account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(String ownerName, BigDecimal initialBalance) {
        return accountRepository.save(new Account(ownerName, initialBalance));
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public BigDecimal debit(Long id, BigDecimal amount, String referenceId) {
        int updatedRows = accountRepository.debit(id, amount);
        if (updatedRows == 0) {
            // 0건이 영향받은 이유가 "계정 없음"인지 "잔액 부족"인지 구분해서 정확한 상태코드를 내려준다.
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new AccountNotFoundException(id));
            log.warn("Debit rejected: insufficient balance. accountId={}, amount={}, balance={}, referenceId={}",
                    id, amount, account.getBalance(), referenceId);
            throw new InsufficientBalanceException(id);
        }
        log.info("Debit applied. accountId={}, amount={}, referenceId={}", id, amount, referenceId);
        return accountRepository.findById(id).orElseThrow().getBalance();
    }

    @Transactional
    public BigDecimal credit(Long id, BigDecimal amount, String referenceId) {
        int updatedRows = accountRepository.credit(id, amount);
        if (updatedRows == 0) {
            throw new AccountNotFoundException(id);
        }
        log.info("Credit applied. accountId={}, amount={}, referenceId={}", id, amount, referenceId);
        return accountRepository.findById(id).orElseThrow().getBalance();
    }
}
