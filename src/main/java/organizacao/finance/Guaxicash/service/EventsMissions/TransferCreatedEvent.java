package organizacao.finance.Guaxicash.service.EventsMissions;

import java.util.UUID;

public record TransferCreatedEvent(UUID userId, UUID transferId, double amount, boolean toSavings) {
}
