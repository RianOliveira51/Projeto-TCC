package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.MissionsCompleted;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionsCompletedRepository extends JpaRepository<MissionsCompleted, UUID> {

    // usa navegação por propriedade: user.uuid e missions.uuid
    boolean existsByUser_UuidAndMissions_Uuid(UUID userId, UUID missionId);

    List<MissionsCompleted> findByUser_Uuid(UUID userId);

    // MissionsCompletedRepository.java
    @Modifying
    @Transactional
    void deleteByUser_Uuid(UUID userId);
}
