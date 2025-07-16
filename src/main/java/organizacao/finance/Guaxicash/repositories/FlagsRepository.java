package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Flags;

import java.util.UUID;

public interface FlagsRepository extends JpaRepository<Flags, UUID> {
}
