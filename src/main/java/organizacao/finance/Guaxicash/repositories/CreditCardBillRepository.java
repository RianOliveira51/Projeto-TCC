package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, UUID> {
    List<CreditCardBill> findByCreditCard_Accounts_User(User user);
    Optional<CreditCardBill> findByUuidAndCreditCard_Accounts_User_Uuid(UUID id, UUID userId);
}
