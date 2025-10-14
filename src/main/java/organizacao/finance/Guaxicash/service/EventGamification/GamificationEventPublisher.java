package organizacao.finance.Guaxicash.service.EventGamification;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import organizacao.finance.Guaxicash.service.EventsMissions.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Component
public class GamificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public GamificationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void accountCreated(UUID userId, UUID accountId) {
        publisher.publishEvent(new AccountCreatedEvent(userId, accountId));
    }

    public void expenseCreated(UUID userId, UUID expenseId, LocalDate date) {
        publisher.publishEvent(new ExpenseCreatedEvent(userId, expenseId, date));
    }

    public void incomeCreated(UUID userId, UUID incomeId, LocalDate date) {
        publisher.publishEvent(new IncomeCreatedEvent(userId, incomeId, date));
    }

    public void transferCreated(UUID userId, UUID transferId, double amount, boolean toSavings) {
        publisher.publishEvent(new TransferCreatedEvent(userId, transferId, amount, toSavings));
    }

    public void billPaid(UUID userId, UUID billId, YearMonth cycle, boolean paidBeforeDue) {
        publisher.publishEvent(new BillPaidEvent(userId, billId, cycle, paidBeforeDue));
    }
}
