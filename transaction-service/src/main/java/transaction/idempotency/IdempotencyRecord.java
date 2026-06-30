package transaction.idempotency;

public record IdempotencyRecord(String status, Integer httpStatus, String bodyJson) {

    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";

    public static IdempotencyRecord inProgress() {
        return new IdempotencyRecord(IN_PROGRESS, null, null);
    }

    public static IdempotencyRecord completed(int httpStatus, String bodyJson) {
        return new IdempotencyRecord(COMPLETED, httpStatus, bodyJson);
    }

    public boolean isInProgress() {
        return IN_PROGRESS.equals(status);
    }
}
