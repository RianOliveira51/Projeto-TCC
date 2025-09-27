package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.Enums.Active;

import java.util.List;
import java.util.UUID;

public interface TypeRepository extends JpaRepository<Type, UUID> {

    // Filtro por status
    List<Type> findAllByActive(Active active);
}
