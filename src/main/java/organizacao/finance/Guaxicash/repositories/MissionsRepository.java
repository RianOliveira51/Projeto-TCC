package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Missions;

import java.util.Optional;
import java.util.UUID;

public interface MissionsRepository extends JpaRepository <Missions, UUID> {
    Optional<Missions> findByTitle(String title);
}
