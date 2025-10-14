package organizacao.finance.Guaxicash.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Enums.Rank;
import organizacao.finance.Guaxicash.entities.Missions;
import organizacao.finance.Guaxicash.entities.MissionsCompleted;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.MissionsCompletedRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;

import java.util.UUID;

@Service
public class GamificationAwardService {
    private final MissionsCompletedRepository missionsCompletedRepo;
    private final UserRepository userRepo;

    public GamificationAwardService(MissionsCompletedRepository missionsCompletedRepo,
                                    UserRepository userRepo) {
        this.missionsCompletedRepo = missionsCompletedRepo;
        this.userRepo = userRepo;
    }

    /** Abre SEMPRE uma nova transação. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeAndGrantXp(UUID userId, Missions mission) {
        User user = userRepo.findById(userId).orElseThrow();

        MissionsCompleted mc = new MissionsCompleted();
        mc.setUser(user);
        mc.setMissions(mission);
        missionsCompletedRepo.save(mc);

        int novoXp = user.getXP() + mission.getValue();
        user.setXP(novoXp);
        user.setRank(Rank.fromXp(novoXp));

        userRepo.saveAndFlush(user); // força UPDATE imediato
    }
}
