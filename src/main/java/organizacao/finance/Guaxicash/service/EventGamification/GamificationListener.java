package organizacao.finance.Guaxicash.service.EventGamification;


import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import organizacao.finance.Guaxicash.service.EventsMissions.*;
import organizacao.finance.Guaxicash.service.MissionsCompletedService;
import java.time.Year;


@Component

public class GamificationListener {

    private final MissionsCompletedService missions;

    public GamificationListener(MissionsCompletedService missions) {
        this.missions = missions;
    }
    /* ===================== DESPESA ===================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ExpenseCreatedEvent e) {
        // Missão 1: 1 conta + (2 despesas + 1 receita)
        missions.checkMission1AndComplete(e.userId());

        // Missão 2: registrar gastos todos os dias durante a semana
        missions.checkMission2AndComplete(e.userId(), e.date());

        // Missão 8 (opcional disparar aqui): “< 10 despesas no mês”
        // Se quiser verificar continuamente, use o mês da despesa:
        // missions.checkMission8AndComplete(e.userId(), YearMonth.from(e.date()));
        // (ou deixe para um scheduler no fim do mês)
    }

    /* ===================== RECEITA ===================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncomeCreatedEvent e) {
        // Missão 1: 1 conta + (2 despesas + 1 receita)
        missions.checkMission1AndComplete(e.userId());

        // Missão 6: 3 receitas no mesmo dia
        missions.checkMission6AndComplete(e.userId(), e.date());

        // Missão 10: 50 receitas no ano
        missions.checkMission10AndComplete(e.userId(), Year.from(e.date()));
    }

    /* ===================== TRANSFERÊNCIA ===================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TransferCreatedEvent e) {
        // Missão 3: transferir >= 500 para poupança
        missions.checkMission3AndComplete(e.userId(), e.amount(), e.toSavings());
    }

    /* ===================== CONTA CRIADA ===================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AccountCreatedEvent e) {
        // Missão 7: criar conta do tipo poupança
        missions.checkMission7AndComplete(e.userId(), e.isSavings());
        // (Missão 1 continua sendo checada pelos eventos de receita/despesa)
    }

    /* ===================== FATURA PAGA ===================== */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BillPaidEvent e) {
        // Missão 4: 3 meses seguidos pagando 100% antes do vencimento
        missions.checkMission4AndComplete(e.userId(), e.cycle(), e.paidBeforeDue());

        // Missão 9 (opcional disparar via scheduler no fechamento do mês):
        // mês sem compras no cartão de crédito
        // missions.checkMission9AndComplete(e.userId(), e.cycle());
    }
}
