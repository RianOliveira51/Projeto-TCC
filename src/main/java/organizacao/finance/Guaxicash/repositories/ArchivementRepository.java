package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import organizacao.finance.Guaxicash.entities.Archivement;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArchivementRepository extends JpaRepository<Archivement, UUID> {
    Optional<Archivement> findByTitle(String title);
}
