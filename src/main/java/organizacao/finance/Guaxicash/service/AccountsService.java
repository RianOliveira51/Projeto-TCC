package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
import organizacao.finance.Guaxicash.service.EventGamification.GamificationEventPublisher;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountsService {

    @Autowired private AccountsRepository accountsRepository;
    @Autowired private BankRepository bankRepository;
    @Autowired private TypeRepository typeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SecurityService securityService;


    @Autowired private CreditCardRepository creditCardRepository;
    @Autowired private BillRepository billRepository;


    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private RecipheRepository recipheRepository;
    @Autowired private TransactionsRepository transactionsRepository;

    @Autowired private ExpenseService expenseService;
    @Autowired private RecipheService recipheService;
    @Autowired private TransactionsService transactionsService;

    @Autowired private GamificationEventPublisher gamificationEventPublisher;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertActive(Accounts acc) {
        if (acc.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Conta desativada. Operação não permitida.");
        }
    }
    private void assertTypeActive(Type type) {
        if (type.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Tipo desativado. Não é possível vincular a conta.");
        }
    }

    public List<Accounts> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? accountsRepository.findAll()
                : accountsRepository.findByUser(me);
    }

    public List<Accounts> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? accountsRepository.findAllByActive(active)
                : accountsRepository.findByUserAndActive(me, active);
    }


    public Accounts findById(UUID id) {
        User me = securityService.obterUserLogin();
        Accounts acc = accountsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (!isAdmin(me) && !acc.getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("Você não tem permissão para ver esta conta.");
        }
        return acc;
    }

    // ===== CRUD
    public Accounts insert(Accounts accounts) {
        UUID bankId = accounts.getBank().getUuid();
        UUID typeId = accounts.getType().getUuid();

        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundExeption(bankId));
        Type type = typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundExeption(typeId));

        User me = securityService.obterUserLogin();
        User user = userRepository.findById(me.getUuid())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuário não encontrado"));

        accounts.setUser(user);
        accounts.setBank(bank);
        accounts.setType(type);
        if (accounts.getActive() == null) accounts.setActive(Active.ACTIVE);

        if (accounts.getActive() != null && accounts.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar conta já desativada.");
        }
        assertTypeActive(type);
        accounts.setActive(Active.ACTIVE);
        gamificationEventPublisher.accountCreated(accounts.getUser().getUuid(), accounts.getUuid());

        return accountsRepository.save(accounts);
    }

    public Accounts update(UUID id, Accounts updatedAccount) {
        try {
            Accounts entity = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            User me = securityService.obterUserLogin();
            if (!isAdmin(me) && !entity.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Você não tem permissão para atualizar esta conta.");
            }

            entity.setName(updatedAccount.getName());
            entity.setBalance(updatedAccount.getBalance());

            if (updatedAccount.getBank() != null && updatedAccount.getBank().getUuid() != null) {
                Bank bank = bankRepository.findById(updatedAccount.getBank().getUuid())
                        .orElseThrow(() -> new ResourceNotFoundExeption(updatedAccount.getBank().getUuid()));
                entity.setBank(bank);
            }
            if (updatedAccount.getType() != null && updatedAccount.getType().getUuid() != null) {
                Type type = typeRepository.findById(updatedAccount.getType().getUuid())
                        .orElseThrow(() -> new ResourceNotFoundExeption(updatedAccount.getType().getUuid()));
                assertTypeActive(type);
                entity.setType(type);
            }
            assertActive(entity);
            return accountsRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    public void delete(UUID id){
        try {
            Accounts entity = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
            User me = securityService.obterUserLogin();
            if (!isAdmin(me) && !entity.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Você não tem permissão para deletar esta conta.");
            }
            accountsRepository.delete(entity);
        } catch (ResourceNotFoundExeption e) {
            throw new ResourceNotFoundExeption(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public List<Accounts> findByUser(UUID userId) {
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !me.getUuid().equals(userId)) {
            throw new AccessDeniedException("Sem permissão para consultar contas de outro usuário.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExeption(userId));
        return accountsRepository.findByUser(user);
    }

    public Double totalBalanceOfLogged() {
        User me = securityService.obterUserLogin();
        var stream = (isAdmin(me) ? accountsRepository.findAll().stream()
                : accountsRepository.findByUser(me).stream())
                .map(Accounts::getBalance)
                .filter(Objects::nonNull);
        double sum = stream.mapToDouble(Double::doubleValue).sum();
        return BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // ===== Toggle + Cascata =====
    @Transactional
    public void deactivate(UUID id) { setActiveWithCascade(id, Active.DISABLE); }

    @Transactional
    public void activate(UUID id) { setActiveWithCascade(id, Active.ACTIVE); }

    private void setActiveWithCascade(UUID accountId, Active target) {
        Accounts entity = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundExeption(accountId));
        User me = securityService.obterUserLogin();

        boolean owner = entity.getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para alterar esta conta.");

        // Já está no estado desejado? Idempotente
        if (entity.getActive() == target) return;

        // 1) account
        entity.setActive(target);
        accountsRepository.save(entity);

        // 2) Cartões e Bills como você já fazia
        List<CreditCard> cards = creditCardRepository.findByAccounts(entity);
        for (CreditCard c : cards) c.setActive(target);
        creditCardRepository.saveAll(cards);

        if (!cards.isEmpty()) {
            List<Bill> bills = billRepository.findByCreditCardIn(cards);
            for (Bill b : bills) b.setActive(target);
            billRepository.saveAll(bills);
        }

        // 3) NOVO: Cascata financeira p/ lançamentos ligados à conta
        if (target == Active.DISABLE) {
            // 3.1 Expenses (estorna no saldo)
            List<Expenses> expList = expenseRepository.findByAccountsAndActive(entity, Active.ACTIVE);
            for (Expenses e : expList) {
                expenseService.deactivate(e.getUuid());
            }

            // 3.2 Reciphe (estorna no saldo)
            List<Reciphe> recList = recipheRepository.findByAccountsAndActive(entity, Active.ACTIVE);
            for (Reciphe r : recList) {
                recipheService.deactivate(r.getUuid());
            }

            // 3.3 Transactions (sem afetar saldo)
            List<Transactions> txFrom = transactionsRepository.findByAccountsAndActive(entity, Active.ACTIVE);
            for (Transactions t : txFrom) {
                transactionsService.deactivateSilently(t.getUuid());
            }
            List<Transactions> txTo = transactionsRepository.findByForaccountsAndActive(entity, Active.ACTIVE);
            for (Transactions t : txTo) {
                transactionsService.deactivateSilently(t.getUuid());
            }
        }
        // OBS: No activate(...) não reativamos lançamentos automaticamente (mantemos do jeito que está).
    }
}
