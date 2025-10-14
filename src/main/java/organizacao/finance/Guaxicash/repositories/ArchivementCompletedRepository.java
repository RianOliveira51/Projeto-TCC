package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import organizacao.finance.Guaxicash.entities.ArchivementCompleted;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArchivementCompletedRepository extends JpaRepository<ArchivementCompleted, UUID> {

    boolean existsByUser_UuidAndArchivement_Uuid(UUID userId, UUID archivementId);

    List<ArchivementCompleted> findByUser_Uuid(UUID userId);
}
