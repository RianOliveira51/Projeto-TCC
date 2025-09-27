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
import organizacao.finance.Guaxicash.repositories.ExpenseRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private AccountsRepository accountsRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SecurityService securityService;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertActive(Expenses e) {
        if (e.getActive() != Active.ACTIVE) throw new IllegalStateException("Despesa desativada. Operação não permitida.");
    }
    private void assertAccountActive(Accounts acc) {
        if (acc.getActive() != Active.ACTIVE) throw new IllegalStateException("Conta desativada. Operação não permitida.");
    }
    private void assertCategoryActive(Category c) {
        if (c.isActive() != Active.ACTIVE) throw new IllegalStateException("Categoria desativada. Operação não permitida.");
    }

    // ===== READ =====
    public List<Expenses> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? expenseRepository.findAll() : expenseRepository.findByAccounts_User(me);
    }

    public List<Expenses> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? expenseRepository.findAllByActive(active)
                : expenseRepository.findByAccounts_UserAndActive(me, active);
    }

    public Expenses findById(UUID id) {
        User me = securityService.obterUserLogin();
        return isAdmin(me)
                ? expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : expenseRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public List<Expenses> searchBydateRegistration(LocalDate from, LocalDate to) {
        if (from == null && to == null) throw new IllegalArgumentException("Informe ao menos uma data.");
        if (from == null) from = to;
        if (to == null)   to   = from;
        if (to.isBefore(from)) throw new IllegalArgumentException("'to' não pode ser anterior a 'from'.");
        var me = securityService.obterUserLogin();
        return expenseRepository.findByAccounts_User_UuidAndDateRegistrationBetween(
                me.getUuid(), from, to, Sort.by("dateRegistration").ascending()
        );
    }

    // ===== CREATE =====
    @Transactional
    public Expenses insert(Expenses expense) {
        if (expense.getValue() == null || expense.getValue() <= 0f) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        if (expense.getActive() != null && expense.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar despesa já desativada.");
        }

        UUID categoryId = expense.getCategory().getUuid();
        UUID accountId  = expense.getAccounts().getUuid();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundExeption(categoryId));
        Accounts accounts = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundExeption(accountId));

        // dono ou ADMIN
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !accounts.getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
        }

        // relacionados precisam estar ativos
        assertAccountActive(accounts);
        assertCategoryActive(category);

        expense.setCategory(category);
        expense.setAccounts(accounts);
        expense.setActive(Active.ACTIVE);

        // subtrai do saldo
        addToBalance(accounts, bd(expense.getValue()).negate());
        accountsRepository.save(accounts);

        return expenseRepository.save(expense);
    }

    // ===== UPDATE (estorna antigo e aplica novo) =====
    @Transactional
    public Expenses update(UUID id, Expenses payload) {
        User me = securityService.obterUserLogin();

        Expenses persisted = isAdmin(me)
                ? expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : expenseRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        assertActive(persisted);

        Accounts oldAcc = persisted.getAccounts();
        BigDecimal oldVal = bd(persisted.getValue());

        // Troca de conta
        if (payload.getAccounts() != null && payload.getAccounts().getUuid() != null) {
            Accounts newAcc = accountsRepository.findById(payload.getAccounts().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getAccounts().getUuid()));
            if (!isAdmin(me) && !newAcc.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
            }
            assertAccountActive(newAcc);
            persisted.setAccounts(newAcc);
        }

        // Troca de categoria
        if (payload.getCategory() != null && payload.getCategory().getUuid() != null) {
            Category cat = categoryRepository.findById(payload.getCategory().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));
            assertCategoryActive(cat);
            persisted.setCategory(cat);
        }

        // Campos simples
        if (payload.getDescription() != null) persisted.setDescription(payload.getDescription());
        if (payload.getDateRegistration() != null) persisted.setDateRegistration(payload.getDateRegistration());
        if (payload.getValue() != null) persisted.setValue(payload.getValue());

        BigDecimal newVal = bd(persisted.getValue());
        if (newVal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }

        Accounts newAcc = persisted.getAccounts();

        if (!oldAcc.getUuid().equals(newAcc.getUuid())) {
            addToBalance(oldAcc, oldVal);                 // estorna
            addToBalance(newAcc, newVal.negate());        // aplica
            accountsRepository.save(oldAcc);
            accountsRepository.save(newAcc);
        } else {
            addToBalance(oldAcc, oldVal.subtract(newVal)); // delta
            accountsRepository.save(oldAcc);
        }

        return expenseRepository.save(persisted);
    }

    // ===== HARD DELETE (mantém) =====
    @Transactional
    public void delete(UUID id) {
        User me = securityService.obterUserLogin();

        Expenses entity = isAdmin(me)
                ? expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : expenseRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        Accounts acc = entity.getAccounts();
        addToBalance(acc, bd(entity.getValue())); // estorno (+valor)
        accountsRepository.save(acc);

        expenseRepository.delete(entity);
    }

    // ===== TOGGLES =====
    @Transactional
    public void deactivate(UUID id) {
        Expenses e = findById(id); // respeita dono/admin
        if (e.getActive() == Active.DISABLE) return; // idempotente
        // estorna o efeito (despesa subtraiu; desativar soma de volta)
        Accounts acc = e.getAccounts();
        addToBalance(acc, bd(e.getValue())); // +valor
        accountsRepository.save(acc);
        e.setActive(Active.DISABLE);
        expenseRepository.save(e);
    }

    @Transactional
    public void activate(UUID id) {
        Expenses e = findById(id);
        if (e.getActive() == Active.ACTIVE) return;
        // relacionados ativos
        assertAccountActive(e.getAccounts());
        assertCategoryActive(e.getCategory());
        // reaplica o efeito (subtrai de novo)
        Accounts acc = e.getAccounts();
        addToBalance(acc, bd(e.getValue()).negate());
        accountsRepository.save(acc);
        e.setActive(Active.ACTIVE);
        expenseRepository.save(e);
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
}
