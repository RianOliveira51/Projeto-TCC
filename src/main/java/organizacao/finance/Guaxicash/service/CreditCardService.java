package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.repositories.CreditCardRepository;
import organizacao.finance.Guaxicash.service.eventos.CreditCardForBill.CreditCardCreatedEvent;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private BillService billService;

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

    @Transactional
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

        CreditCard saved = creditCardRepository.save(card);

        //evento SÍNCRONO após salvar (mesma transação)
        publisher.publishEvent(new CreditCardCreatedEvent(this, saved.getUuid()));

        return saved;

    }

    @Transactional
    public CreditCard updateCard(UUID id, CreditCard payload) {
        User me = securityService.obterUserLogin();

        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me)) {
            throw new AccessDeniedException("Você não pode alterar este cartão.");
        }

        CreditCard persisted = creditCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));

        boolean closeChanged  = payload.getCloseDate()  != null
                && !payload.getCloseDate().equals(persisted.getCloseDate());
        boolean expiryChanged = payload.getExpiryDate() != null
                && !payload.getExpiryDate().equals(persisted.getExpiryDate());

        boolean bothDatesChanged = closeChanged && expiryChanged;

        // ---- Atualizar outros campos, se informados ----
        if (payload.getLimite() != null)         persisted.setLimite(payload.getLimite());
        if (payload.getDescription() != null)    persisted.setDescription(payload.getDescription());
        if (payload.getFlags() != null)          persisted.setFlags(payload.getFlags());
        if (payload.getAccounts() != null)       persisted.setAccounts(payload.getAccounts());

        // ---- Atualizar datas apenas se vierem (parcial é permitido) ----
        if (payload.getCloseDate() != null)      persisted.setCloseDate(payload.getCloseDate());
        if (payload.getExpiryDate() != null)     persisted.setExpiryDate(payload.getExpiryDate());

        CreditCard saved = creditCardRepository.save(persisted);

        // ---- Recalendarizar faturas somente se as DUAS datas foram alteradas ----
        if (bothDatesChanged) {
            billService.rescheduleFutureBills(saved);
        }

        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        User me = securityService.obterUserLogin();

        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me)) {
            throw new AccessDeniedException("Você não pode excluir este cartão.");
        }

        try {
            // Fallback explícito (idempotente):
            billRepository.deleteByCreditCardUuid(id);

            // Agora apaga o cartão (se mapeado com cascade remove, também funcionaria sem a linha acima)
            creditCardRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public CreditCardService(CreditCardRepository repo, ApplicationEventPublisher publisher) {
        this.creditCardRepository = repo; this.publisher = publisher;
    }



}
