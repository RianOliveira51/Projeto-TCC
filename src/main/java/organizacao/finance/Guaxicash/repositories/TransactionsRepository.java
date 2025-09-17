package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Transactions;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {

    List<Transactions> findByAccounts_User(User user);

    Optional<Transactions> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Transactions> findByAccounts_User_UuidAndRegistrationDateBetween(
            UUID userId, LocalDate start, LocalDate end, Sort sort
    );
}
