package organizacao.finance.Guaxicash.service.scheduller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.MissionsCompletedService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Component
public class SchedulerMissions {

    private static final Logger log = LoggerFactory.getLogger(SchedulerMissions.class);
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final MissionsCompletedService missions;
    private final UserRepository userRepo;

    public SchedulerMissions(MissionsCompletedService missions, UserRepository userRepo) {
        this.missions = missions;
        this.userRepo = userRepo;
    }

    /** Missão 5: “Não gaste por uma semana” (7 dias sem despesa).
     *  Roda TOD DIA 00:30 checando a janela que termina em “ontem”.
     */
    @Scheduled(cron = "0 30 0 * * *", zone = "America/Sao_Paulo")
    @Transactional(readOnly = true)
    public void checkMission5Daily() {
        LocalDate end = LocalDate.now(ZONE).minusDays(1);
        List<User> users = userRepo.findAll();
        for (User u : users) {
            try {
                missions.checkMission5AndComplete(u.getUuid(), end);
            } catch (Exception ex) {
                log.warn("Falha ao verificar Missão 5 para user={} dataEnd={}: {}",
                        u.getUuid(), end, ex.getMessage());
            }
        }
    }

    /** Missão 9: “Um mês sem gastar no cartão de crédito”.
     *  Roda TOD DIA 1º do mês às 00:15 checando o MÊS ANTERIOR.
     */
    @Scheduled(cron = "0 15 0 1 * *", zone = "America/Sao_Paulo")
    @Transactional(readOnly = true)
    public void checkMission9Monthly() {
        YearMonth lastMonth = YearMonth.now(ZONE).minusMonths(1);
        List<User> users = userRepo.findAll();
        for (User u : users) {
            try {
                missions.checkMission9AndComplete(u.getUuid(), lastMonth);
            } catch (Exception ex) {
                log.warn("Falha ao verificar Missão 9 para user={} month={}: {}",
                        u.getUuid(), lastMonth, ex.getMessage());
            }
        }
    }
}