package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Expenses;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expenses, UUID> {

    List<Expenses> findByAccounts_User(User user);
    Optional<Expenses> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Expenses> findByAccounts_User_UuidAndDateRegistrationBetween(
            UUID userId, LocalDate from, LocalDate to, Sort sort
    );

    // Filtros por status
    List<Expenses> findAllByActive(Active active);
    List<Expenses> findByAccounts_UserAndActive(User user, Active active);

    // (opcional) busca por data + active, se quiser usar no futuro
    List<Expenses> findByAccounts_User_UuidAndActiveAndDateRegistrationBetween(
            UUID userId, Active active, LocalDate from, LocalDate to, Sort sort
    );
    List<Expenses> findByAccountsAndActive(Accounts accounts, Active active);
}
