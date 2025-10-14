package organizacao.finance.Guaxicash.service.EventsMissions;

import java.time.YearMonth;
import java.util.UUID;

public record BillPaidEvent(UUID userId, UUID billId, YearMonth cycle, boolean paidBeforeDue) {
}
