package account.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long accountId) {
        super("Insufficient balance on account: " + accountId);
    }
}
