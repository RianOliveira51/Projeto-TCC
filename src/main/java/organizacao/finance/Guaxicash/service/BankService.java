package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {
    @Autowired
    private BankRepository bankRepository;


    public List<Bank> findAll() {return bankRepository.findAll();
    }

    public Bank findById(UUID id) {
        Optional<Bank> bank = bankRepository.findById(id);
        return bank.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public Bank insert(Bank bank) {
        return bankRepository.save(bank);
    }

    public Bank update(UUID id, Bank updatedBank) {
        try {
            Bank entity = bankRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
            updateData(entity, updatedBank);
            return bankRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Bank entity, Bank updatedAccount) {
        entity.setName(updatedAccount.getName());
    }

    public void delete(UUID id){
        try {
            bankRepository.deleteById(id);
        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

}
