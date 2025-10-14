package organizacao.finance.Guaxicash.service.EventsMissions;

import java.time.LocalDate;
import java.util.UUID;

public record ExpenseCreatedEvent(UUID userId, UUID expenseId, LocalDate date) {
}
