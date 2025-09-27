package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Transactions;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionsRepository extends JpaRepository<Transactions, UUID> {

    List<Transactions> findByAccounts_User(User user);
    Optional<Transactions> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Transactions> findByAccounts_User_UuidAndRegistrationDateBetween(
            UUID userId, LocalDate start, LocalDate end, Sort sort
    );

    // Filtros por status
    List<Transactions> findAllByActive(Active active);
    List<Transactions> findByAccounts_UserAndActive(User user, Active active);

    // ADIÇÕES PARA A CASCATA
    // Transações em que a conta é ORIGEM
    List<Transactions> findByAccountsAndActive(Accounts accounts, Active active);

    // Transações em que a conta é DESTINO (campo 'foraccounts' na entidade)
    List<Transactions> findByForaccountsAndActive(Accounts foraccounts, Active active);

}
