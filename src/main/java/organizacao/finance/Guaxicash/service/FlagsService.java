package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.repositories.FlagsRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class FlagsService {

    @Autowired
    private FlagsRepository flagsRepository;

    private void assertActive(Flags f) {
        if (f.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Bandeira desativada. Operação não permitida.");
        }
    }

    // ===== Listagens
    public List<Flags> findAll() {
        return flagsRepository.findAll();
    }

    public List<Flags> findAll(Active active) {
        return flagsRepository.findAllByActive(active);
    }

    public Flags findById(UUID id) {
        return flagsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // ===== CRUD
    public Flags insert(Flags flags) {
        if (flags.getActive() != null && flags.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar bandeira já desativada.");
        }
        flags.setActive(Active.ACTIVE);
        return flagsRepository.save(flags);
    }

    public Flags update(UUID id, Flags updatedFlags) {
        try {
            Flags entity = flagsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // bloqueia update se desativada
            assertActive(entity);

            updateData(entity, updatedFlags);
            return flagsRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Flags entity, Flags payload) {
        if (payload.getName() != null) {
            entity.setName(payload.getName());
        }
        // não alteramos 'active' aqui; use os endpoints de toggle
    }

    /** Hard delete (controller limita a ADMIN) */
    public void delete(UUID id) {
        try {
            flagsRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public void deactivate(UUID id) {
        Flags f = flagsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (f.getActive() == Active.DISABLE) return; // idempotente
        f.setActive(Active.DISABLE);
        flagsRepository.save(f);
    }

    public void activate(UUID id) {
        Flags f = flagsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (f.getActive() == Active.ACTIVE) return; // idempotente
        f.setActive(Active.ACTIVE);
        flagsRepository.save(f);
    }
}
