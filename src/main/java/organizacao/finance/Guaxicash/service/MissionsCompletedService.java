package organizacao.finance.Guaxicash.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.repositories.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class MissionsCompletedService {

    private static final UUID MISSION1_ID = UUID.fromString("0ee96869-4a28-415b-8355-f324de1ec62d");
    private static final UUID MISSION2_ID = UUID.fromString("aa5a9074-55f2-4338-8252-f4de794a9c1c");
    private static final UUID MISSION3_ID = UUID.fromString("b190357e-5343-4162-853b-e6b6690aa0cc");
    private static final UUID MISSION4_ID = UUID.fromString("bbc64770-1218-442f-b126-8b1be1cb453e");

    private final MissionsRepository missionsRepo;
    private final MissionsCompletedRepository missionsCompletedRepo;
    private final AccountsRepository accountsRepo;
    private final ExpenseRepository expenseRepo;
    private final RecipheRepository recipheRepo;
    private final BillRepository billRepo;

    private final GamificationAwardService awardService; // <-- novo

    public MissionsCompletedService(
            MissionsRepository missionsRepo,
            MissionsCompletedRepository missionsCompletedRepo,
            AccountsRepository accountsRepo,
            ExpenseRepository expenseRepo,
            RecipheRepository recipheRepo,
            BillRepository billRepo,
            GamificationAwardService awardService
    ) {
        this.missionsRepo = missionsRepo;
        this.missionsCompletedRepo = missionsCompletedRepo;
        this.accountsRepo = accountsRepo;
        this.expenseRepo = expenseRepo;
        this.recipheRepo = recipheRepo;
        this.billRepo = billRepo;
        this.awardService = awardService;
    }

    /* ========= Missão 1 ========= */
    @Transactional(readOnly = true)
    public boolean checkMission1AndComplete(UUID userId) {
        var mission = getMission(MISSION1_ID);
        if (alreadyDone(userId, mission)) return false;

        long contasAtivas = accountsRepo.countActiveByUser(userId);
        long despesas = expenseRepo.countByAccounts_User_Uuid(userId);
        long receitas = recipheRepo.countByAccounts_User_Uuid(userId);

        if (contasAtivas >= 1 && despesas >= 2 && receitas >= 1) {
            awardService.completeAndGrantXp(userId, mission);
            return true;
        }
        return false;
    }

    /* ========= Missão 2 ========= */
    @Transactional(readOnly = true)
    public boolean checkMission2AndComplete(UUID userId, double amount, boolean toSavings) {
        if (!toSavings || amount < 500.00) return false;
        var mission = getMission(MISSION2_ID);
        if (alreadyDone(userId, mission)) return false;

        awardService.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= Missão 3 ========= */
    @Transactional(readOnly = true)
    public boolean checkMission3AndComplete(UUID userId, YearMonth cycleJustPaid, boolean paidBeforeDueNow) {
        var mission = getMission(MISSION3_ID);
        if (alreadyDone(userId, mission)) return false;

        if (!paidBeforeDueNow) return false;

        var last3 = billRepo.findLastPaidBills(userId, PageRequest.of(0, 3));
        if (last3.size() < 3) return false;

        var ym0 = YearMonth.from(last3.get(0).getCloseDate());
        var ym1 = YearMonth.from(last3.get(1).getCloseDate());
        var ym2 = YearMonth.from(last3.get(2).getCloseDate());

        boolean consecutivos = ym0.equals(cycleJustPaid)
                && ym1.equals(ym0.minusMonths(1))
                && ym2.equals(ym0.minusMonths(2));
        if (!consecutivos) return false;

        awardService.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= Missão 4 ========= */
    @Transactional(readOnly = true)
    public boolean checkMission4AndComplete(UUID userId, LocalDate endInclusive) {
        var mission = getMission(MISSION4_ID);
        if (alreadyDone(userId, mission)) return false;

        var start = endInclusive.minusDays(6);
        for (var d = start; !d.isAfter(endInclusive); d = d.plusDays(1)) {
            if (expenseRepo.countInDay(userId, d) == 0) return false;
        }
        awardService.completeAndGrantXp(userId, mission);
        return true;
    }

    /* ========= Helpers ========= */
    private organizacao.finance.Guaxicash.entities.Missions getMission(UUID id) {
        return missionsRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Missão não encontrada: " + id));
    }

    private boolean alreadyDone(UUID userId, organizacao.finance.Guaxicash.entities.Missions mission) {
        return missionsCompletedRepo.existsByUser_UuidAndMissions_Uuid(userId, mission.getUuid());
    }
}
