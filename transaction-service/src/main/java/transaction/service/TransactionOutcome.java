package transaction.service;

import transaction.dto.TransactionResponse;

public record TransactionOutcome(int httpStatus, TransactionResponse body) {
}
