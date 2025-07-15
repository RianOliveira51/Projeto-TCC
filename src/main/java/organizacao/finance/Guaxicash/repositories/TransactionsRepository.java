package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Transactions;

public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
}
