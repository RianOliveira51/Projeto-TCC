package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.CreditCard;

import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

}
