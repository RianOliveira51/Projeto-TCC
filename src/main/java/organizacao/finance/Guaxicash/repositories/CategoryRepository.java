package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Category;
import organizacao.finance.Guaxicash.entities.Enums.Active;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByEarn(boolean earn);

    // novos filtros com Active
    List<Category> findAllByActive(Active active);
    List<Category> findByEarnAndActive(boolean earn, Active active);
}
