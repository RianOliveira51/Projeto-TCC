package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    // por dono
    List<CreditCard> findAllByAccounts_User(User user);
    Optional<CreditCard> findByUuidAndAccounts_User(UUID id, User user);
    boolean existsByUuidAndAccounts_User(UUID id, User user);

    // por conta
    List<CreditCard> findByAccounts(Accounts accounts);

    // filtros por active
    List<CreditCard> findAllByAccounts_UserAndActive(User user, Active active);
    List<CreditCard> findByAccountsAndActive(Accounts accounts, Active active);

    @Modifying
    @Transactional
    void deleteByAccounts_User_Uuid(UUID userId);
}
