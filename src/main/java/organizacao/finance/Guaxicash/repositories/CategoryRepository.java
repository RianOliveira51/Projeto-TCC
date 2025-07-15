package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
