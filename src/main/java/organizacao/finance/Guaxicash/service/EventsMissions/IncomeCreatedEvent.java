package organizacao.finance.Guaxicash.service.EventsMissions;

import java.time.LocalDate;
import java.util.UUID;

public record IncomeCreatedEvent(UUID userId, UUID incomeId, LocalDate date) {
}
