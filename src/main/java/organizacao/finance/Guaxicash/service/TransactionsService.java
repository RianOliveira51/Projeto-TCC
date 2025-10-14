package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.CategoryRepository;
import organizacao.finance.Guaxicash.repositories.TransactionsRepository;
import organizacao.finance.Guaxicash.service.EventGamification.GamificationEventPublisher;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionsService {

    @Autowired private TransactionsRepository transactionsRepository;
    @Autowired private AccountsRepository accountsRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SecurityService securityService;
    @Autowired private GamificationEventPublisher gamificationEventPublisher;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertTxActive(Transactions t) {
        if (t.getActive() != Active.ACTIVE) throw new IllegalStateException("Transação desativada. Operação não permitida.");
    }
    private void assertAccountActive(Accounts acc) {
        if (acc.getActive() != Active.ACTIVE) throw new IllegalStateException("Conta desativada. Operação não permitida.");
    }
    private void assertCategoryActiveIfPresent(Category c) {
        if (c != null && c.isActive() != Active.ACTIVE) {
            throw new IllegalStateException("Categoria desativada. Operação não permitida.");
        }
    }

    // ========= READ =========
    public List<Transactions> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? transactionsRepository.findAll()
                : transactionsRepository.findByAccounts_User(me);
    }

    public List<Transactions> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? transactionsRepository.findAllByActive(active)
                : transactionsRepository.findByAccounts_UserAndActive(me, active);
    }

    public Transactions findById(UUID id) {
        User me = securityService.obterUserLogin();
        Optional<Transactions> opt = isAdmin(me)
                ? transactionsRepository.findById(id)
                : transactionsRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid());
        return opt.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public List<Transactions> searchByRegistrationDate(LocalDate from, LocalDate to) {
        if (from == null && to == null) throw new IllegalArgumentException("Informe ao menos uma data.");
        if (from == null) from = to;
        if (to == null)   to   = from;
        if (to.isBefore(from)) throw new IllegalArgumentException("'to' não pode ser anterior a 'from'.");

        var me = securityService.obterUserLogin();

        return transactionsRepository.findByAccounts_User_UuidAndRegistrationDateBetween(
                me.getUuid(), from, to, Sort.by("registrationDate").ascending()
        );
    }

    // ========= CREATE =========
    @Transactional
    public Transactions insert(Transactions tx) {
        validarValorPositivo(tx.getValue());
        if (tx.getActive() != null && tx.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar transação já desativada.");
        }

        UUID fromId = tx.getAccounts().getUuid();
        UUID toId   = tx.getForaccounts().getUuid();
        UUID catId  = tx.getCategory() != null ? tx.getCategory().getUuid() : null;

        Accounts from = accountsRepository.findById(fromId)
                .orElseThrow(() -> new ResourceNotFoundExeption(fromId));
        Accounts to   = accountsRepository.findById(toId)
                .orElseThrow(() -> new ResourceNotFoundExeption(toId));

        assertAccountActive(from);
        assertAccountActive(to);

        if (from.getUuid().equals(to.getUuid())) {
            throw new IllegalArgumentException("Conta de origem e destino não podem ser a mesma.");
        }

        User me = securityService.obterUserLogin();
        if (!isAdmin(me)) {
            if (!from.getUser().getUuid().equals(me.getUuid()) || !to.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Você só pode transferir entre suas próprias contas.");
            }
        }

        if (catId != null) {
            Category cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundExeption(catId));
            assertCategoryActiveIfPresent(cat);
            tx.setCategory(cat);
        }

        tx.setAccounts(from);
        tx.setForaccounts(to);
        tx.setActive(Active.ACTIVE);

        BigDecimal amount = bd(tx.getValue());
        addToBalance(from, amount.negate());
        addToBalance(to,   amount);

        accountsRepository.save(from);
        accountsRepository.save(to);

        boolean toSavings =
                tx.getForaccounts().getType() != null
                        && tx.getForaccounts().getType().getDescription() != null
                        && tx.getForaccounts().getType().getDescription().equalsIgnoreCase("Poupança");
        gamificationEventPublisher.transferCreated(
                tx.getAccounts().getUser().getUuid(),
                tx.getUuid(),
                tx.getValue().doubleValue(),
                toSavings
        );
        return transactionsRepository.save(tx);
    }

    @Transactional
    public Transactions update(UUID id, Transactions payload) {
        Transactions persisted = transactionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        assertTxActive(persisted);

        User me = securityService.obterUserLogin();
        if (!isAdmin(me)) {
            if (!persisted.getAccounts().getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Sem permissão para alterar esta transferência.");
            }
        }

        Accounts oldFrom = accountsRepository.findById(persisted.getAccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(persisted.getAccounts().getUuid()));
        Accounts oldTo   = accountsRepository.findById(persisted.getForaccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(persisted.getForaccounts().getUuid()));
        BigDecimal oldAmount = bd(persisted.getValue());

        // estorna efeito antigo
        addToBalance(oldFrom, oldAmount);
        addToBalance(oldTo,   oldAmount.negate());
        accountsRepository.save(oldFrom);
        accountsRepository.save(oldTo);

        if (payload.getAccounts() == null || payload.getAccounts().getUuid() == null)
            throw new IllegalArgumentException("Conta de origem é obrigatória.");
        if (payload.getForaccounts() == null || payload.getForaccounts().getUuid() == null)
            throw new IllegalArgumentException("Conta de destino é obrigatória.");
        validarValorPositivo(payload.getValue());

        Accounts newFrom = accountsRepository.findById(payload.getAccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getAccounts().getUuid()));
        Accounts newTo   = accountsRepository.findById(payload.getForaccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getForaccounts().getUuid()));

        assertAccountActive(newFrom);
        assertAccountActive(newTo);

        if (!isAdmin(me)) {
            if (!newFrom.getUser().getUuid().equals(me.getUuid()) || !newTo.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Você só pode transferir entre suas próprias contas.");
            }
        }
        if (newFrom.getUuid().equals(newTo.getUuid())) {
            throw new IllegalArgumentException("Conta de origem e destino não podem ser a mesma.");
        }

        if (payload.getCategory() != null && payload.getCategory().getUuid() != null) {
            Category cat = categoryRepository.findById(payload.getCategory().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));
            assertCategoryActiveIfPresent(cat);
            persisted.setCategory(cat);
        }

        persisted.setDescription(payload.getDescription());
        persisted.setRegistrationDate(payload.getRegistrationDate());
        persisted.setValue(payload.getValue());
        persisted.setAccounts(newFrom);
        persisted.setForaccounts(newTo);

        BigDecimal newAmount = bd(persisted.getValue());
        addToBalance(newFrom, newAmount.negate());
        addToBalance(newTo,   newAmount);

        accountsRepository.save(newFrom);
        accountsRepository.save(newTo);

        return transactionsRepository.save(persisted);
    }

    // ========= HARD DELETE =========
    @Transactional
    public void delete(UUID id) {
        Transactions tx = transactionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        User me = securityService.obterUserLogin();
        if (!isAdmin(me)) {
            if (!tx.getAccounts().getUser().getUuid().equals(me.getUuid())
                    || !tx.getForaccounts().getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Sem permissão para excluir esta transferência.");
            }
        }

        Accounts from = accountsRepository.findById(tx.getAccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(tx.getAccounts().getUuid()));
        Accounts to   = accountsRepository.findById(tx.getForaccounts().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(tx.getForaccounts().getUuid()));
        BigDecimal amount = bd(tx.getValue());

        addToBalance(from, amount);
        addToBalance(to,   amount.negate());

        accountsRepository.save(from);
        accountsRepository.save(to);

        transactionsRepository.delete(tx);
    }

    // ========= TOGGLES =========
    @Transactional
    public void deactivateSilently(UUID id) {
        Transactions t = findById(id); // respeita owner/admin conforme já implementado
        if (t.getActive() == Active.DISABLE) return;
        t.setActive(Active.DISABLE);
        transactionsRepository.save(t);
    }

    @Transactional
    public void activate(UUID id) {
        Transactions t = findById(id);
        if (t.getActive() == Active.ACTIVE) return;
        Accounts from = t.getAccounts();
        Accounts to   = t.getForaccounts();

        assertAccountActive(from);
        assertAccountActive(to);
        assertCategoryActiveIfPresent(t.getCategory());

        BigDecimal amount = bd(t.getValue());
        // Aplica novamente a transferência
        addToBalance(from, amount.negate());
        addToBalance(to,   amount);
        accountsRepository.save(from);
        accountsRepository.save(to);
        t.setActive(Active.ACTIVE);
        transactionsRepository.save(t);
    }

    private static BigDecimal bd(Float v) {
        double d = (v == null ? 0.0 : v.doubleValue());
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }
    private void addToBalance(Accounts acc, BigDecimal delta) {
        BigDecimal cur = BigDecimal.valueOf(acc.getBalance() == null ? 0.0 : acc.getBalance())
                .setScale(2, RoundingMode.HALF_UP);
        acc.setBalance(cur.add(delta).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }
    private static void validarValorPositivo(Float v) {
        if (v == null || v <= 0f) throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
    }
}
