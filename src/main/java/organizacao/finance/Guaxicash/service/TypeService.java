package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TypeService {

    @Autowired
    private TypeRepository typeRepository;

    private void assertActive(Type t) {
        if (t.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Tipo desativado. Operação não permitida.");
        }
    }

    // ===== Listagens
    public List<Type> findAll() {
        return typeRepository.findAll();
    }

    public List<Type> findAll(Active active) {
        return typeRepository.findAllByActive(active);
    }

    public Type findById(UUID id) {
        Optional<Type> type = typeRepository.findById(id);
        return type.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // ===== CRUD
    public Type insert(Type type) {
        if (type.getActive() != null && type.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar tipo já desativado.");
        }
        type.setActive(Active.ACTIVE);
        return typeRepository.save(type);
    }

    public Type update(UUID id, Type updatedType) {
        try {
            Type entity = typeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // bloqueia update se desativado
            assertActive(entity);

            updateData(entity, updatedType);
            return typeRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Type entity, Type payload) {
        if (payload.getDescription() != null) {
            entity.setDescription(payload.getDescription());
        }
        // não alteramos 'active' aqui; use os endpoints de toggle
    }

    /** Hard delete (controller restringe a ADMIN) */
    public void delete(UUID id){
        try {
            typeRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public void deactivate(UUID id) {
        Type t = typeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (t.getActive() == Active.DISABLE) return; // idempotente
        t.setActive(Active.DISABLE);
        typeRepository.save(t);
    }

    public void activate(UUID id) {
        Type t = typeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (t.getActive() == Active.ACTIVE) return; // idempotente
        t.setActive(Active.ACTIVE);
        typeRepository.save(t);
    }
}
