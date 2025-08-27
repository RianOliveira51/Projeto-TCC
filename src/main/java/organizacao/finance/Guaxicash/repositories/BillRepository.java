package organizacao.finance.Guaxicash.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Bill;

import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

}
