package organizacao.finance.Guaxicash.service.EventGamification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import organizacao.finance.Guaxicash.service.EventsMissions.*;
import organizacao.finance.Guaxicash.service.MissionsCompletedService;

@Component
public class GamificationListener {

    private final MissionsCompletedService missions;

    public GamificationListener(MissionsCompletedService missions) {
        this.missions = missions;
    }

    // Missão 1
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AccountCreatedEvent e) { missions.checkMission1AndComplete(e.userId()); }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ExpenseCreatedEvent e) {
        missions.checkMission1AndComplete(e.userId());
        missions.checkMission4AndComplete(e.userId(), e.date()); // 7 dias seguidos
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncomeCreatedEvent e) { missions.checkMission1AndComplete(e.userId()); }

    // Missão 2
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransferCreatedEvent e) { missions.checkMission2AndComplete(e.userId(), e.amount(), e.toSavings()); }

    // Missão 3
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BillPaidEvent e) { missions.checkMission3AndComplete(e.userId(), e.cycle(), e.paidBeforeDue()); }
}
