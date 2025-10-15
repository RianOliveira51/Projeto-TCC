package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Archivement;
import organizacao.finance.Guaxicash.entities.ArchivementCompleted;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.dto.ArchivementResponse;
import organizacao.finance.Guaxicash.entities.dto.CompleteArchivementResponse;
import organizacao.finance.Guaxicash.repositories.ArchivementCompletedRepository;
import organizacao.finance.Guaxicash.repositories.ArchivementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchivementService {

    @Autowired
    private ArchivementRepository archivementRepository;
    @Autowired
    private ArchivementCompletedRepository completedRepository;
    @Autowired
    private SecurityService securityService;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    /** UUIDs fixos das 10 conquistas existentes no banco (do seu JSON) */
    private static final class Refs {
        static final UUID ONE_ACCOUNT_MONTH   = UUID.fromString("117af391-5653-4f7a-bd53-003487f338f7");
        static final UUID ADD_CREDIT_CARD     = UUID.fromString("2046c62f-a733-4428-992d-fec710593815");
        static final UUID RESERVE_10K         = UUID.fromString("27a179f3-175d-4fb8-905e-0d4fef8ba857");
        static final UUID THREE_BANKS         = UUID.fromString("2af2c0d2-7be9-48c1-8c14-a2118f00b03c");
        static final UUID CREATE_USER         = UUID.fromString("5188c2d9-855e-4c5d-ad49-69afbf761e6d");
        static final UUID CC_BILL_10_OR_LESS  = UUID.fromString("59df4845-4ac1-413f-a823-aaf94bd53d3c");
        static final UUID ALL_MISSIONS_DONE   = UUID.fromString("6dca814c-371d-4253-9a45-a3b41b3dc4c9");
        static final UUID LEISURE_LT_500      = UUID.fromString("c96eb41d-8fd4-4a22-9b02-cd21f9a499be");
        static final UUID FOUR_GREEN_MONTHS   = UUID.fromString("d08bb0a7-4c34-45f7-9c0d-e149cac5e1b6");
        static final UUID CC_SPEND_LT_500     = UUID.fromString("e49a7d22-758c-4f01-8ade-693651c2e3e0");
    }

    // ========= CRUD básico =========
    public Archivement insert(Archivement archivement) {
        return archivementRepository.save(archivement);
    }

    public List<Archivement> findAll() {
        return archivementRepository.findAll();
    }

    // ========= Listagens com status =========
    public List<ArchivementResponse> listAllWithUserStatus() {
        User me = securityService.obterUserLogin();

        var completedMap = completedRepository.findByUser_Uuid(me.getUuid())
                .stream()
                .collect(Collectors.toMap(
                        ac -> ac.getArchivement().getUuid(),
                        ArchivementCompleted::getCompletedAt
                ));

        return archivementRepository.findAll()
                .stream()
                .map(a -> new ArchivementResponse(
                        a.getUuid(),
                        a.getTitle(),
                        a.getDescription(),
                        completedMap.containsKey(a.getUuid()),
                        completedMap.get(a.getUuid()) // null quando não concluída
                ))
                .toList();
    }

    public User getLoggedUser() {
        return securityService.obterUserLogin();
    }

    public List<ArchivementResponse> listCompleted() {
        User me = securityService.obterUserLogin();
        return completedRepository.findByUser_Uuid(me.getUuid())
                .stream()
                .map(ac -> new ArchivementResponse(
                        ac.getArchivement().getUuid(),
                        ac.getArchivement().getTitle(),
                        ac.getArchivement().getDescription(),
                        true,
                        ac.getCompletedAt()))
                .toList();
    }

    public long trophiesCount() {
        User me = securityService.obterUserLogin();
        return completedRepository.countByUser_Uuid(me.getUuid());
    }

    // ========= Completar manualmente (idempotente) =========
    @Transactional
    public CompleteArchivementResponse complete(UUID archivementId) {
        User me = securityService.obterUserLogin();

        if (completedRepository.existsByUser_UuidAndArchivement_Uuid(me.getUuid(), archivementId)) {
            LocalDate when = completedRepository.findByUser_Uuid(me.getUuid()).stream()
                    .filter(ac -> ac.getArchivement().getUuid().equals(archivementId))
                    .map(ArchivementCompleted::getCompletedAt)
                    .findFirst().orElse(null);
            long total = completedRepository.countByUser_Uuid(me.getUuid());
            return new CompleteArchivementResponse(archivementId, true, when, total);
        }

        Archivement a = archivementRepository.findById(archivementId)
                .orElseThrow(() -> new NoSuchElementException("Archivement não encontrado"));

        completedRepository.save(new ArchivementCompleted(null, me, a));
        long total = completedRepository.countByUser_Uuid(me.getUuid());
        return new CompleteArchivementResponse(archivementId, false, LocalDate.now(), total);
    }

    @Transactional
    public void onUserCreated(User newUser) {
        mark(newUser, Refs.CREATE_USER); // idempotente
    }

    // ========= ENGINE de regras =========
    @Transactional
    public void evaluateAllForMe(YearMonth ref) {
        evaluateAllFor(securityService.obterUserLogin(), ref);
    }

    @Transactional
    public void evaluateAllFor(User u, YearMonth ref) {
        // (1) 4 meses seguidos com gasto < receita
        if (isFourGreenMonths(u, ref)) mark(u, Refs.FOUR_GREEN_MONTHS);

        // (2) Reserva de emergência >= 10.000
        if (nvl(getEmergencyReserve(u)).compareTo(new BigDecimal("10000")) >= 0)
            mark(u, Refs.RESERVE_10K);

        // (3) 3 contas em bancos distintos
        if (countDistinctBanks(u) >= 3)
            mark(u, Refs.THREE_BANKS);

        // (4) <= 10 despesas no cartão E fatura fechada — somente se houve despesa (>0)
        {
            int ccCount = getCreditCardMonthExpenseCount(u, ref);
            if (isAnyBillClosedForMonth(u, ref) && ccCount > 0 && ccCount <= 10) {
                mark(u, Refs.CC_BILL_10_OR_LESS);
            }
        }

        // (5) Cartão < 500 no mês — somente se houve gasto (>0)
        {
            BigDecimal ccTotal = getCreditCardMonthTotal(u, ref);
            if (ccTotal != null
                    && ccTotal.compareTo(BigDecimal.ZERO) > 0
                    && ccTotal.compareTo(new BigDecimal("500")) < 0) {
                // opcional: exigir fatura fechada também
                // if (isAnyBillClosedForMonth(u, ref)) { mark(u, Refs.CC_SPEND_LT_500); }
                mark(u, Refs.CC_SPEND_LT_500);
            }
        }

        // (6) Gastar em apenas uma conta no mês
        if (spentUsingSingleAccount(u, ref))
            mark(u, Refs.ONE_ACCOUNT_MONTH);

        // (7) Lazer < 500 no mês — somente se houve gasto (>0)
        {
            BigDecimal lazer = getMonthlyExpenseByCategory(u, ref, "LAZER");
            if (lazer != null
                    && lazer.compareTo(BigDecimal.ZERO) > 0
                    && lazer.compareTo(new BigDecimal("500")) < 0) {
                mark(u, Refs.LEISURE_LT_500);
            }
        }

        // (8) Criou usuário (sempre marcamos — idempotente)
        mark(u, Refs.CREATE_USER);

        // (9) Cadastrou cartão
        if (countCreditCards(u) >= 1)
            mark(u, Refs.ADD_CREDIT_CARD);

        // (10) Fez todas as missões
        if (allMissionsCompleted(u))
            mark(u, Refs.ALL_MISSIONS_DONE);
    }

    private boolean isFourGreenMonths(User u, YearMonth endInclusive) {
        YearMonth m = endInclusive;
        for (int i = 0; i < 4; i++) {
            BigDecimal inc = nvl(getMonthlyIncome(u, m));
            BigDecimal exp = nvl(getMonthlyExpense(u, m));
            if (inc.compareTo(exp) <= 0) return false;
            m = m.minusMonths(1);
        }
        return true;
    }

    private void mark(User u, UUID archId) {
        if (!completedRepository.existsByUser_UuidAndArchivement_Uuid(u.getUuid(), archId)) {
            Archivement a = archivementRepository.findById(archId)
                    .orElseThrow(() -> new NoSuchElementException("Archivement não encontrado: " + archId));
            completedRepository.save(new ArchivementCompleted(null, u, a));
        }
    }

    private BigDecimal nvl(BigDecimal v) { return (v == null) ? BigDecimal.ZERO : v; }

    // ==========================
    // HELPERS “inline” (TODOs)
    // Substitua por consultas reais aos seus repositórios.
    // ==========================

    private BigDecimal getMonthlyIncome(User u, YearMonth ym) {
        // TOD: somar receitas do usuário no mês ym
        return BigDecimal.ZERO;
    }

    private BigDecimal getMonthlyExpense(User u, YearMonth ym) {
        // TOD: somar despesas do usuário no mês ym
        return BigDecimal.ZERO;
    }

    private BigDecimal getMonthlyExpenseByCategory(User u, YearMonth ym, String categoryName) {
        // TOD: somar despesas da categoria no mês ym; se não houver, retorne null
        return null;
    }

    private BigDecimal getCreditCardMonthTotal(User u, YearMonth ym) {
        // TOD: somar gastos de cartão no mês ym; se não houver, retorne null
        return null;
    }

    private int getCreditCardMonthExpenseCount(User u, YearMonth ym) {
        // TOD: contar lançamentos do cartão no mês ym; 0 quando não houver
        return 0;
    }

    private boolean isAnyBillClosedForMonth(User u, YearMonth ym) {
        // TOD: verificar se existe fatura fechada no mês ym
        return false;
    }

    private int countDistinctBanks(User u) {
        // TOD: retornar quantidade de bancos distintos das contas do usuário
        return 0;
    }

    private boolean spentUsingSingleAccount(User u, YearMonth ym) {
        // TOD: true se todas as despesas do mês forem da mesma conta
        return false;
    }

    private BigDecimal getEmergencyReserve(User u) {
        // TOD: saldo/valor na conta de “reserva de emergência”
        return BigDecimal.ZERO;
    }

    private int countCreditCards(User u) {
        // TOD: quantidade de cartões cadastrados
        return 0;
    }

    private boolean allMissionsCompleted(User u) {
        // TOD: verificar se todas as missões ativas (ou do período) estão concluídas
        return false;
    }

    @Transactional
    public void onCreditCardCreated(User user) {
        mark(user, Refs.ADD_CREDIT_CARD);
    }
}
