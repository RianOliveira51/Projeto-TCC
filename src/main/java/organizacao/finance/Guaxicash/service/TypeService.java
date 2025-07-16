package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TypeService {

    @Autowired
    private TypeRepository typeRepository;


    public List<Type> findAll() {return typeRepository.findAll();
    }

    public Type findById(UUID id) {
        Optional<Type> type = typeRepository.findById(id);
        return type.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public Type insert(Type type) {
        return typeRepository.save(type);
    }

    public void Delete(UUID id){
        try {
            typeRepository.deleteById(id);
        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }
}
