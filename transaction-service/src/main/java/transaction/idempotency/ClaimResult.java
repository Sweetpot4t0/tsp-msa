package transaction.idempotency;

public record ClaimResult(boolean claimed, IdempotencyRecord existing) {

    public static ClaimResult acquired() {
        return new ClaimResult(true, null);
    }

    public static ClaimResult existing(IdempotencyRecord record) {
        return new ClaimResult(false, record);
    }
}
