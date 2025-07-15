package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Accounts;

import java.util.UUID;

public interface AccountsRepository extends JpaRepository<Accounts, UUID> {
}
