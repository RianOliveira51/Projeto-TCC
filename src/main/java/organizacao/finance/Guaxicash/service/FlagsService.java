package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.repositories.FlagsRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class FlagsService {
    @Autowired
    private FlagsRepository flagsRepository;

    public List<Flags> findAll() {return flagsRepository.findAll();
    }

    public Flags insert(Flags flags) {
        return flagsRepository.save(flags);
    }

    public Flags update(UUID id, Flags updatedFlgas) {
        try {
            Flags entity = flagsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
            updateData(entity, updatedFlgas);
            return flagsRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Flags entity, Flags updatedAccount) {
        entity.setName(updatedAccount.getName());
    }

    public void delete(UUID id){
        try {
            flagsRepository.deleteById(id);
        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }
}
