package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Reciphe;

import java.util.UUID;

public interface RecipheRepository extends JpaRepository<Reciphe, UUID> {

}
