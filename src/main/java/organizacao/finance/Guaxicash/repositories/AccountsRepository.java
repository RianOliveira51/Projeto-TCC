package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.UUID;

public interface AccountsRepository extends JpaRepository<Accounts, UUID> {
    List<Accounts> findByUser(User user);

    // filtros por active
    List<Accounts> findAllByActive(Active active);
    List<Accounts> findByUserAndActive(User user, Active active);
}
