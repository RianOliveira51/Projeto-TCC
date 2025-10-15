package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.ArchivementCompleted;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArchivementCompletedRepository extends JpaRepository<ArchivementCompleted, UUID> {

    boolean existsByUser_UuidAndArchivement_Uuid(UUID userId, UUID archivementId);

    List<ArchivementCompleted> findByUser_Uuid(UUID userId);

    long countByUser_Uuid(UUID userId);

    @Modifying
    @Transactional
    void deleteByUser_Uuid(UUID userId);
}
