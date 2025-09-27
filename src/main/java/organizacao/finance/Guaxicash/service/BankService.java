package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankService {

    @Autowired
    private BankRepository bankRepository;

    private void assertActive(Bank bank) {
        if (bank.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Banco desativado. Operação não permitida.");
        }
    }

    // ===== Listagens
    public List<Bank> findAll() {
        return bankRepository.findAll();
    }

    public List<Bank> findAll(Active active) {
        return bankRepository.findAllByActive(active);
    }

    public Bank findById(UUID id) {
        Optional<Bank> bank = bankRepository.findById(id);
        return bank.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // ===== CRUD
    public Bank insert(Bank bank) {
        if (bank.getActive() != null && bank.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar banco já desativado.");
        }
        bank.setActive(Active.ACTIVE);
        return bankRepository.save(bank);
    }

    public Bank update(UUID id, Bank updatedBank) {
        try {
            Bank entity = bankRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // Não permite atualizar nome se banco está desativado
            assertActive(entity);

            updateData(entity, updatedBank);
            return bankRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Bank entity, Bank updatedBank) {
        entity.setName(updatedBank.getName());
        // não alteramos 'active' aqui; use os endpoints de toggle
    }

    /** Hard delete (controller limita a ADMIN) */
    public void delete(UUID id) {
        try {
            bankRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public void deactivate(UUID id) {
        Bank b = bankRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (b.getActive() == Active.DISABLE) return; // idempotente
        b.setActive(Active.DISABLE);
        bankRepository.save(b);
    }

    public void activate(UUID id) {
        Bank b = bankRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (b.getActive() == Active.ACTIVE) return; // idempotente
        b.setActive(Active.ACTIVE);
        bankRepository.save(b);
    }
}
