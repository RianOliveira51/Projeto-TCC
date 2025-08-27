package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.repositories.CreditCardRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    public List<CreditCard> findAll() {return creditCardRepository.findAll();
    }

    public CreditCard insert(CreditCard CreditCard) {
        return creditCardRepository.save(CreditCard);
    }

    public CreditCard update(UUID id, CreditCard updatedCreditCard) {
        try {
            CreditCard entity = creditCardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
            updateData(entity, updatedCreditCard);
            return creditCardRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(CreditCard entity, CreditCard updatedCreditCard) {
        entity.setLimite(updatedCreditCard.getLimite());
        entity.setDescription(updatedCreditCard.getDescription());
        entity.setCloseDate(updatedCreditCard.getCloseDate());
        entity.setExpiryDate(updatedCreditCard.getExpiryDate());
        entity.setFlags(updatedCreditCard.getFlags());
        entity.setAccounts(updatedCreditCard.getAccounts());

        if (updatedCreditCard.getFlags() != null) {
            entity.setFlags(updatedCreditCard.getFlags());
        }

        if (updatedCreditCard.getAccounts() != null) {
            entity.setAccounts(updatedCreditCard.getAccounts());
        }

    }

    public void delete(UUID id){
        try {
            creditCardRepository.deleteById(id);
        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

}
