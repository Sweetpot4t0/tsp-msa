package transaction.client;

public record ClientCallResult<T>(boolean success, T value, String failureReason) {

    public static <T> ClientCallResult<T> success(T value) {
        return new ClientCallResult<>(true, value, null);
    }

    public static <T> ClientCallResult<T> failure(String reason) {
        return new ClientCallResult<>(false, null, reason);
    }
}
