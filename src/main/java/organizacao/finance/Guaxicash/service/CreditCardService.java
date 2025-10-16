package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
import organizacao.finance.Guaxicash.service.eventos.CreditCardForBill.CreditCardCreatedEvent;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {

    @Autowired private CreditCardRepository creditCardRepository;
    @Autowired private AccountsRepository accountsRepository;
    @Autowired private SecurityService securityService;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private BillRepository billRepository;
    @Autowired private BillService billService;
    @Autowired private ArchivementService archivementService;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertActive(Accounts acc) {
        if (acc.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Conta desativada. Operação não permitida para cartões.");
        }
    }
    private void assertActive(CreditCard card) {
        if (card.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Cartão desativado. Operação não permitida.");
        }
    }

    public List<CreditCard> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? creditCardRepository.findAll()
                : creditCardRepository.findAllByAccounts_User(me);
    }

    public List<CreditCard> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? creditCardRepository.findAll().stream()
                .filter(c -> c.getActive() == active).toList()
                : creditCardRepository.findAllByAccounts_UserAndActive(me, active);
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

    // ===== CRUD
    @Transactional
    public CreditCard insert(CreditCard card) {
        User me = securityService.obterUserLogin();

        if (card.getAccounts() == null || card.getAccounts().getUuid() == null)
            throw new IllegalArgumentException("O campo 'accounts' é obrigatório.");

        Accounts acc = accountsRepository.findById(card.getAccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(card.getAccounts().getUuid()));

        if (!isAdmin(me) && !acc.getUser().getUuid().equals(me.getUuid()))
            throw new AccessDeniedException("A conta informada não pertence ao usuário autenticado.");

        if (card.getActive() == null) card.setActive(Active.ACTIVE);
        card.setAccounts(acc);
        assertActive(acc);                  // conta precisa estar ativa
        if (card.getActive() != null && card.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar cartão já desativado.");
        }
        card.setActive(Active.ACTIVE);
        CreditCard saved = creditCardRepository.save(card);
        publisher.publishEvent(new CreditCardCreatedEvent(this, saved.getUuid()));
        archivementService.onCreditCardCreated(me);
        return saved;
    }

    @Transactional
    public CreditCard updateCard(UUID id, CreditCard payload) {
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me))
            throw new AccessDeniedException("Você não pode alterar este cartão.");

        CreditCard persisted = creditCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado"));

        // ---- BLOQUEIO de redução de limite abaixo do valor
        if (payload.getLimite() != null) {
            double novoLimite = payload.getLimite();
            // soma(value - valuepay) de todas as faturas do cartão com status != PAID
            double emAberto = billRepository.findByCreditCard(persisted).stream()
                    .filter(b -> b.getStatus() != BillPay.PAID)
                    .map(b -> {
                        double total = b.getValue() == null ? 0.0 : b.getValue();
                        double pago  = b.getValuepay() == null ? 0.0 : b.getValuepay();
                        return BigDecimal.valueOf(total - pago).setScale(2, RoundingMode.HALF_UP);
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue();

            if (novoLimite < emAberto) {
                throw new IllegalStateException(
                        "Não é permitido reduzir o limite abaixo do valor já utilizado em faturas (" + String.format(java.util.Locale.US, "%.2f", emAberto) + ")."
                );
            }
            persisted.setLimite(novoLimite);
        }

        boolean closeChanged  = payload.getCloseDate()  != null && !payload.getCloseDate().equals(persisted.getCloseDate());
        boolean expiryChanged = payload.getExpiryDate() != null && !payload.getExpiryDate().equals(persisted.getExpiryDate());

        if (payload.getDescription() != null) persisted.setDescription(payload.getDescription());
        if (payload.getFlags() != null)       persisted.setFlags(payload.getFlags());
        if (payload.getAccounts() != null)    persisted.setAccounts(payload.getAccounts());
        if (payload.getCloseDate() != null)   persisted.setCloseDate(payload.getCloseDate());
        if (payload.getExpiryDate() != null)  persisted.setExpiryDate(payload.getExpiryDate());

        CreditCard saved = creditCardRepository.save(persisted);

        // ---- REAGENDAR faturas futuras quando QUALQUER uma das datas mudar
        if (closeChanged || expiryChanged) {
            billService.rescheduleFutureBills(saved);
        }
        return saved;
    }

    // Hard delete
    @Transactional
    public void delete(UUID id) {
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !creditCardRepository.existsByUuidAndAccounts_User(id, me))
            throw new AccessDeniedException("Você não pode excluir este cartão.");
        try {
            billRepository.deleteByCreditCardUuid(id); // apaga faturas do cartão
            creditCardRepository.deleteById(id);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    // Toggle + cascata para Bills
    @Transactional public void deactivate(UUID id) { setActiveWithCascade(id, Active.DISABLE); }
    @Transactional public void activate(UUID id)   { setActiveWithCascade(id, Active.ACTIVE);  }

    private void setActiveWithCascade(UUID cardId, Active target) {
        User me = securityService.obterUserLogin();
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundExeption(cardId));

        boolean owner = card.getAccounts() != null &&
                card.getAccounts().getUser() != null &&
                card.getAccounts().getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para alterar este cartão.");

        if (target == Active.ACTIVE && card.getAccounts().getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Conta desativada. Ative a conta antes de ativar o cartão.");
        }

        card.setActive(target);
        creditCardRepository.save(card);

        // Cascateia o mesmo status para as faturas
        List<Bill> bills = billRepository.findByCreditCard(card);
        for (Bill b : bills) b.setActive(target);
        billRepository.saveAll(bills);
    }
}
