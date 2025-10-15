package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import organizacao.finance.Guaxicash.entities.Missions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissionsRepository extends JpaRepository <Missions, UUID> {
    Optional<Missions> findByTitle(String title);

    @Query("""
       select m from Missions m
        where m.uuid not in (
            select mc.missions.uuid
            from MissionsCompleted mc
            where mc.user.uuid = :userId
        )
       """)
    List<Missions> findAllNotCompletedByUser(@Param("userId") UUID userId);
}
