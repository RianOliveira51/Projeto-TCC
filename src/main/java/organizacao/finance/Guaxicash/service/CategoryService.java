package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Category;
import organizacao.finance.Guaxicash.repositories.CategoryRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() {return categoryRepository.findAll();
    }

    public Category findById(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // novo: filtrar por earn (true=receber, false=despesa)
    public List<Category> findByEarn(boolean earn) {
        return categoryRepository.findByEarn(earn);
    }

    public Category insert(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(UUID id, Category updatedCategory) {
        try {
            Category entity = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
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
        // permite atualizar o earn (despesa/receber)
        entity.setEarn(updated.isEarn());
    }

    public void delete(UUID id){
        try {
            categoryRepository.deleteById(id);
        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

}
