package organizacao.finance.Guaxicash.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    boolean existsByCreditCardAndCloseDate(CreditCard creditCard, LocalDate closeDate);

    List<Bill> findByCreditCardUuid(UUID creditCardId);

    List<Bill> findByCreditCardAccountsUserUuid(UUID userId);

    long deleteByCreditCardUuid(UUID creditCardId);
}
