package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Reciphe;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipheRepository extends JpaRepository<Reciphe, UUID> {

    List<Reciphe> findByAccounts_User(User user);
    Optional<Reciphe> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Reciphe> findByAccounts_User_UuidAndDateRegistrationBetween(
            UUID userId, LocalDate start, LocalDate end, Sort sort
    );

    // Filtros por status
    List<Reciphe> findAllByActive(Active active);
    List<Reciphe> findByAccounts_UserAndActive(User user, Active active);

    // Para cascata por conta + status
    List<Reciphe> findByAccountsAndActive(Accounts accounts, Active active);

    long countByAccounts_User_Uuid(UUID userId);

    // ===== SOMAS (TOTAL) =====
    @Query("""
           select coalesce(sum(r.value), 0)
             from Reciphe r
            where r.accounts.user.uuid = :userId
           """)
    Double sumValueByUserId(UUID userId);

    @Query("""
           select coalesce(sum(r.value), 0)
             from Reciphe r
            where r.accounts.user.uuid = :userId
              and r.active = :active
           """)
    Double sumValueByUserIdAndActive(UUID userId, Active active);
}
