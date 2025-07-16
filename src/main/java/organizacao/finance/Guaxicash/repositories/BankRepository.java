package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Bank;

import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

}
