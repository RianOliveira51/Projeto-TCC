package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Expenses;

import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expenses, UUID> {
}
