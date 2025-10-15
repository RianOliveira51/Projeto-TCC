package organizacao.finance.Guaxicash.service.EventsMissions;

import java.util.UUID;

public record AccountCreatedEvent(UUID userId, UUID accountId, boolean isSavings) {
}
