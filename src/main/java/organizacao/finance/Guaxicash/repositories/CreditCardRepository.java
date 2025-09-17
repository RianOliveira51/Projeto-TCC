package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    List<CreditCard> findAllByAccounts_User(User user);

    Optional<CreditCard> findByUuidAndAccounts_User(UUID id, User user);

    boolean existsByUuidAndAccounts_User(UUID id, User user);

}
