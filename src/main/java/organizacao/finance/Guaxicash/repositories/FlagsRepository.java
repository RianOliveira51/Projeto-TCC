package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.entities.Enums.Active;

import java.util.List;
import java.util.UUID;

public interface FlagsRepository extends JpaRepository<Flags, UUID> {

    // Filtro por status
    List<Flags> findAllByActive(Active active);
}
