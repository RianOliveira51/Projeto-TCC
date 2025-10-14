package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
