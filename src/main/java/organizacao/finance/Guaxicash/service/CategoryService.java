package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Category;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.repositories.CategoryRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    private void assertActive(Category c) {
        if (c.isActive() != Active.ACTIVE) {
            throw new IllegalStateException("Categoria desativada. Operação não permitida.");
        }
    }

    // ===== Listagens
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAll(Active active) {
        return categoryRepository.findAllByActive(active);
    }

    public Category findById(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // já existia
    public List<Category> findByEarn(boolean earn) {
        return categoryRepository.findByEarn(earn);
    }

    // novo: earn + active
    public List<Category> findByEarn(boolean earn, Active active) {
        return categoryRepository.findByEarnAndActive(earn, active);
    }

    // ===== CRUD
    public Category insert(Category category) {
        if (category.isActive() != null && category.isActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar categoria já desativada.");
        }
        category.setActive(Active.ACTIVE);
        return categoryRepository.save(category);
    }

    public Category update(UUID id, Category updatedCategory) {
        try {
            Category entity = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // bloqueia update se desativada
            assertActive(entity);

            updateData(entity, updatedCategory);
            return categoryRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Category entity, Category updated) {
        if (updated.getDescription() != null) {
            entity.setDescription(updated.getDescription());
        }
        entity.setEarn(updated.isEarn());
        // não alteramos 'active' aqui; use os endpoints de toggle
    }

    /** Hard delete (controller já restringe a ADMIN) */
    public void delete(UUID id){
        try {
            categoryRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public void deactivate(UUID id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (c.isActive() == Active.DISABLE) return; // idempotente
        c.setActive(Active.DISABLE);
        categoryRepository.save(c);
    }

    public void activate(UUID id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (c.isActive() == Active.ACTIVE) return; // idempotente
        c.setActive(Active.ACTIVE);
        categoryRepository.save(c);
    }
}
