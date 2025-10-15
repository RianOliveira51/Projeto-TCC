package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Enums.Rank;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.entities.MissionsCompleted;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class MissionsCompletedService {

    // ===== UUIDs exatamente como estão no banco =====
    private static final UUID M1_CADASTRAR_CONTA_E_3_LANC = UUID.fromString("0ee96869-4a28-415b-8355-f324de1ec62d");
    private static final UUID M2_GASTAR_TODOS_DIAS_SEMANA = UUID.fromString("bbc64770-1218-442f-b126-8b1be1cb453e");
    private static final UUID M3_TRANSFERIR_500_P_Poup    = UUID.fromString("aa5a9074-55f2-4338-8252-f4de794a9c1c");
    private static final UUID M4_PAGAR_FATURAS_ANTES_3M   = UUID.fromString("b190357e-5343-4162-853b-e6b6690aa0cc");
    private static final UUID M5_SEM_GASTOS_7_DIAS        = UUID.fromString("4dcf614f-9055-42de-afbe-5732d3a29696");
    private static final UUID M6_3_RECEITAS_MESMO_DIA     = UUID.fromString("a08dc02e-3f19-47f5-877b-dda6385fdf0d");
    private static final UUID M7_CRIAR_CONTA_POUPANCA     = UUID.fromString("f2f0eb9b-7094-4935-93db-337d9e7a2c1d");
    private static final UUID M8_MENOS_10_DESPESAS_MES    = UUID.fromString("14e839aa-2bb0-4500-bf17-d4174c6dbcf9");
    private static final UUID M9_UM_MES_SEM_GASTAR_CC     = UUID.fromString("84b53cd8-c247-4163-bfdb-16aec42e2759");
    private static final UUID M10_50_RECEITAS_NO_ANO      = UUID.fromString("e2884495-d71a-4778-85d1-5de9ee32913e");

    private final MissionsRepository missionsRepo;
    private final MissionsCompletedRepository missionsCompletedRepo;
    private final AccountsRepository accountsRepo;
    private final ExpenseRepository expenseRepo;
    private final RecipheRepository recipheRepo;
    private final BillRepository billRepo;
    private final CreditCardBillRepository ccBillRepo;
    private final UserRepository userRepo;

    // IMPORTANTÍSSIMO: auto-injeção do proxy para evitar self-invocation
    @Autowired @Lazy
    private MissionsCompletedService self;

    public MissionsCompletedService(
            MissionsRepository missionsRepo,
            MissionsCompletedRepository missionsCompletedRepo,
            AccountsRepository accountsRepo,
            ExpenseRepository expenseRepo,
            RecipheRepository recipheRepo,
            BillRepository billRepo,
            CreditCardBillRepository ccBillRepo,
            UserRepository userRepo
    ) {
        this.missionsRepo = missionsRepo;
        this.missionsCompletedRepo = missionsCompletedRepo;
        this.accountsRepo = accountsRepo;
        this.expenseRepo = expenseRepo;
        this.recipheRepo = recipheRepo;
        this.billRepo = billRepo;
        this.ccBillRepo = ccBillRepo;
        this.userRepo = userRepo;
    }

    /* ========= util ========= */
    private Missions getMission(UUID id) {
        return missionsRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Missão não encontrada: " + id));
    }

    private boolean alreadyDone(UUID userId, Missions mission) {
        return missionsCompletedRepo.existsByUser_UuidAndMissions_Uuid(userId, mission.getUuid());
    }

    /** Concede XP e registra a missão numa NOVA transação. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeAndGrantXp(UUID userId, Missions mission) {
        User user = userRepo.findById(userId).orElseThrow();

        MissionsCompleted mc = new MissionsCompleted();
        mc.setUser(user);
        mc.setMissions(mission);
        missionsCompletedRepo.save(mc);

        int novoXp = user.getXP() + mission.getValue(); // getXP() = int (0 por padrão)
        user.setXP(novoXp);
        user.setRank(Rank.fromXp(novoXp));
        userRepo.save(user);
    }

    /* ========= M1 ========= */
    @Transactional
    public boolean checkMission1AndComplete(UUID userId) {
        Missions mission = getMission(M1_CADASTRAR_CONTA_E_3_LANC);
        if (alreadyDone(userId, mission)) return false;

        long contasAtivas = accountsRepo.countActiveByUser(userId);
        long despesas = expenseRepo.countBetween(userId, LocalDate.MIN, LocalDate.MAX);
        long receitas = recipheRepo.countBetween(userId, LocalDate.MIN, LocalDate.MAX);

        if (contasAtivas >= 1 && despesas >= 2 && receitas >= 1) {
            self.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }

    /* ========= M2 ========= */
    @Transactional
    public boolean checkMission2AndComplete(UUID userId, LocalDate endInclusive) {
        Missions mission = getMission(M2_GASTAR_TODOS_DIAS_SEMANA);
        if (alreadyDone(userId, mission)) return false;

        LocalDate start = endInclusive.minusDays(6);
        for (LocalDate d = start; !d.isAfter(endInclusive); d = d.plusDays(1)) {
            if (expenseRepo.countInDay(userId, d) == 0) return false;
        }
        self.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= M3 ========= */
    @Transactional
    public boolean checkMission3AndComplete(UUID userId, double amount, boolean toSavings) {
        if (!toSavings || amount < 500.00) return false;
        Missions mission = getMission(M3_TRANSFERIR_500_P_Poup);
        if (alreadyDone(userId, mission)) return false;

        self.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= M4 ========= */
    @Transactional
    public boolean checkMission4AndComplete(UUID userId, YearMonth cycleJustPaid, boolean paidBeforeDueNow) {
        Missions mission = getMission(M4_PAGAR_FATURAS_ANTES_3M);
        if (alreadyDone(userId, mission)) return false;
        if (!paidBeforeDueNow) return false;

        var last3 = billRepo.findLastPaidBills(userId, PageRequest.of(0, 3));
        if (last3.size() < 3) return false;

        YearMonth ym0 = YearMonth.from(last3.get(0).getCloseDate());
        YearMonth ym1 = YearMonth.from(last3.get(1).getCloseDate());
        YearMonth ym2 = YearMonth.from(last3.get(2).getCloseDate());

        boolean consecutivos = ym0.equals(cycleJustPaid)
                && ym1.equals(ym0.minusMonths(1))
                && ym2.equals(ym0.minusMonths(2));
        if (!consecutivos) return false;

        self.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= M5 ========= */
    @Transactional
    public boolean checkMission5AndComplete(UUID userId, LocalDate endInclusive) {
        Missions mission = getMission(M5_SEM_GASTOS_7_DIAS);
        if (alreadyDone(userId, mission)) return false;

        LocalDate start = endInclusive.minusDays(6);
        for (LocalDate d = start; !d.isAfter(endInclusive); d = d.plusDays(1)) {
            if (expenseRepo.countInDay(userId, d) > 0) return false;
        }
        self.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= M6 ========= */
    @Transactional
    public boolean checkMission6AndComplete(UUID userId, LocalDate dayOfIncome) {
        Missions mission = getMission(M6_3_RECEITAS_MESMO_DIA);
        if (alreadyDone(userId, mission)) return false;

        long recs = recipheRepo.countInDay(userId, dayOfIncome);
        if (recs >= 3) {
            self.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }

    /* ========= M7 ========= */
    @Transactional
    public boolean checkMission7AndComplete(UUID userId, boolean createdSavingsAccount) {
        if (!createdSavingsAccount) return false;
        Missions mission = getMission(M7_CRIAR_CONTA_POUPANCA);
        if (alreadyDone(userId, mission)) return false;

        self.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= M8 ========= */
    @Transactional
    public boolean checkMission8AndComplete(UUID userId, YearMonth month) {
        Missions mission = getMission(M8_MENOS_10_DESPESAS_MES);
        if (alreadyDone(userId, mission)) return false;

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        long count = expenseRepo.countBetween(userId, start, end);
        if (count < 10) {
            self.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }

    /* ========= M9 ========= */
    @Transactional
    public boolean checkMission9AndComplete(UUID userId, YearMonth month) {
        Missions mission = getMission(M9_UM_MES_SEM_GASTAR_CC);
        if (alreadyDone(userId, mission)) return false;

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        long ccCount = ccBillRepo.countByUserBetween(userId, start, end);
        if (ccCount == 0) {
            self.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }

    /* ========= M10 ========= */
    @Transactional
    public boolean checkMission10AndComplete(UUID userId, Year year) {
        Missions mission = getMission(M10_50_RECEITAS_NO_ANO);
        if (alreadyDone(userId, mission)) return false;

        LocalDate start = year.atDay(1);
        LocalDate end = year.atMonth(12).atEndOfMonth();
        long total = recipheRepo.countBetween(userId, start, end);
        if (total >= 50) {
            self.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }
}
