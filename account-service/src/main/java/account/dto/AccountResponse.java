package account.dto;

import account.entity.Account;

import java.math.BigDecimal;

public record AccountResponse(Long id, String ownerName, BigDecimal balance) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerName(), account.getBalance());
    }
}
