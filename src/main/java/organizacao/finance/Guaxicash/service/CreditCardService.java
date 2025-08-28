package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.User;
// ajuste o import abaixo conforme o seu enum


import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.CreditCardRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    @Autowired private CreditCardRepository creditCardRepository;
    @Autowired private AccountsRepository accountsRepository;
    @Autowired private SecurityService securityService;

    private boolean isAdmin(User me) {
        return me.getRole() == UserRole.ADMIN;
    }

    public List<CreditCard> findAll() {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) {
            return creditCardRepository.findAll();
        }
        return creditCardRepository.findAllByAccounts_User(me);
    }

    public CreditCard findById(UUID id) {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) {
            return creditCardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
        }
        return creditCardRepository.findByUuidAndAccounts_User(id, me)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public CreditCard insert(CreditCard card) {
        User me = securityService.obterUserLogin();

        if (card.getAccounts() == null || card.getAccounts().getUuid() == null) {
            throw new IllegalArgumentException("O campo 'accounts' é obrigatório.");
        }

        UUID accountId = card.getAccounts().getUuid();

        Accounts acc = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundExeption(accountId));

        if (!isAdmin(me) && !acc.getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("A conta informada não pertence ao usuário autenticado.");
        }

        card.setAccounts(acc);

        return creditCardRepository.save(card);
    }


    public CreditCard update(UUID id, CreditCard updated) {
        User me = securityService.obterUserLogin();

        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me)) {
            throw new AccessDeniedException("Você não pode alterar este cartão.");
        }

        try {
            CreditCard entity = creditCardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // Se trocar a account no payload, admin pode sempre; user comum só se for dele
            if (updated.getAccounts() != null && updated.getAccounts().getUuid() != null) {
                UUID newAccId = updated.getAccounts().getUuid();
                Accounts newAcc = accountsRepository.findById(newAccId)
                        .orElseThrow(() -> new ResourceNotFoundExeption(newAccId));

                if (!isAdmin(me) && !newAcc.getUser().getUuid().equals(me.getUuid())) {
                    throw new AccessDeniedException("A nova conta não pertence ao usuário autenticado.");
                }
                entity.setAccounts(newAcc);
            }

            entity.setLimite(updated.getLimite());
            entity.setDescription(updated.getDescription());
            entity.setCloseDate(updated.getCloseDate());
            entity.setExpiryDate(updated.getExpiryDate());
            if (updated.getFlags() != null) {
                entity.setFlags(updated.getFlags());
            }

            return creditCardRepository.save(entity);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }


    public void delete(UUID id) {
        User me = securityService.obterUserLogin();

        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me)) {
            throw new AccessDeniedException("Você não pode excluir este cartão.");
        }

        try {
            creditCardRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }
}
