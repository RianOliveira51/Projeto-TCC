package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.CreditCardBill;

import java.util.UUID;

public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, UUID> {

}
