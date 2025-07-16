package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Type;

import java.util.UUID;

public interface TypeRepository extends JpaRepository<Type, UUID> {
}
