package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import organizacao.finance.Guaxicash.entities.Expenses;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expenses, UUID> {
    List<Expenses> findByAccounts_User(User user);

    Optional<Expenses> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Expenses> findByAccounts_User_UuidAndDateRegistrationBetween(
            UUID userId, LocalDate from, LocalDate to, Sort sort
    );
}
