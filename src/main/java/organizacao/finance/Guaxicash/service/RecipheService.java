package organizacao.finance.Guaxicash.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
import organizacao.finance.Guaxicash.service.EventGamification.GamificationEventPublisher;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RecipheService {

    @Autowired private RecipheRepository recipheRepository;
    @Autowired private AccountsRepository accountsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SecurityService securityService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private GamificationEventPublisher gamificationEventPublisher;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertActive(Reciphe r) {
        if (r.getActive() != Active.ACTIVE) throw new IllegalStateException("Receita desativada. Operação não permitida.");
    }
    private void assertAccountActive(Accounts acc) {
        if (acc.getActive() != Active.ACTIVE) throw new IllegalStateException("Conta desativada. Operação não permitida.");
    }
    private void assertCategoryActive(Category c) {
        if (c.isActive() != Active.ACTIVE) throw new IllegalStateException("Categoria desativada. Operação não permitida.");
    }

    public List<Reciphe> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? recipheRepository.findAll() : recipheRepository.findByAccounts_User(me);
    }

    public List<Reciphe> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? recipheRepository.findAllByActive(active)
                : recipheRepository.findByAccounts_UserAndActive(me, active);
    }

    public Double totalForCurrentUser(Active active) {
        User me = securityService.obterUserLogin();
        Double total = (active == null)
                ? recipheRepository.sumValueByUserId(me.getUuid())
                : recipheRepository.sumValueByUserIdAndActive(me.getUuid(), active);
        return total == null ? 0.0 : total;
    }

    public Reciphe findById(UUID id) {
        Reciphe r = recipheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !r.getAccounts().getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("Sem permissão para ver este recebimento.");
        }
        return r;
    }

    public List<Reciphe> search(
            String field,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDateTime fromDateTime,
            LocalDateTime toDateTime
    ) {
        User me = securityService.obterUserLogin();

        LocalDate start = (fromDateTime != null) ? fromDateTime.toLocalDate() : fromDate;
        LocalDate end   = (toDateTime != null)   ? toDateTime.toLocalDate()   : toDate;

        if (start == null && end == null) throw new IllegalArgumentException("Informe ao menos uma data (from/to).");
        if (start == null) start = end;
        if (end == null)   end   = start;
        if (end.isBefore(start)) throw new IllegalArgumentException("Parâmetro 'to' não pode ser anterior a 'from'.");

        Sort sort = Sort.by("dateRegistration").ascending();

        return recipheRepository.findByAccounts_User_UuidAndDateRegistrationBetween(
                me.getUuid(), start, end, sort
        );
    }

    @Transactional
    public Reciphe insert(Reciphe reciphe) {
        if (reciphe.getValue() == null || reciphe.getValue() <= 0f) {
            throw new IllegalArgumentException("O valor do recebimento deve ser maior que zero.");
        }
        if (reciphe.getActive() != null && reciphe.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar receita já desativada.");
        }

        UUID categoryId = reciphe.getCategory().getUuid();
        UUID accountId  = reciphe.getAccounts().getUuid();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundExeption(categoryId));
        Accounts accounts = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundExeption(accountId));

        // dono da conta ou ADMIN
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !accounts.getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
        }

        assertAccountActive(accounts);
        assertCategoryActive(category);

        reciphe.setCategory(category);
        reciphe.setAccounts(accounts);
        reciphe.setActive(Active.ACTIVE);

        // soma no saldo
        addToBalance(accounts, bd(reciphe.getValue()));
        accountsRepository.save(accounts);

        gamificationEventPublisher.incomeCreated(
                reciphe.getAccounts().getUser().getUuid(),
                reciphe.getUuid(),
                reciphe.getDateRegistration()
        );

        return recipheRepository.save(reciphe);
    }

    @Transactional
    public Reciphe update(UUID id, Reciphe payload) {
        User me = securityService.obterUserLogin();

        Reciphe persisted = isAdmin(me)
                ? recipheRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : recipheRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        assertActive(persisted);

        Accounts oldAcc = persisted.getAccounts();
        BigDecimal oldVal = bd(persisted.getValue());

        if (payload.getAccounts() != null && payload.getAccounts().getUuid() != null) {
            Accounts newAcc = accountsRepository.findById(payload.getAccounts().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getAccounts().getUuid()));
            if (!isAdmin(me) && !newAcc.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
            }
            assertAccountActive(newAcc);
            persisted.setAccounts(newAcc);
        }

        if (payload.getCategory() != null && payload.getCategory().getUuid() != null) {
            Category cat = categoryRepository.findById(payload.getCategory().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));
            assertCategoryActive(cat);
            persisted.setCategory(cat);
        }

        if (payload.getDescription() != null)      persisted.setDescription(payload.getDescription());
        if (payload.getDateRegistration() != null) persisted.setDateRegistration(payload.getDateRegistration());
        if (payload.getValue() != null)            persisted.setValue(payload.getValue());

        BigDecimal newVal = bd(persisted.getValue());
        if (newVal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do recebimento deve ser maior que zero.");
        }

        Accounts newAcc = persisted.getAccounts();

        if (!oldAcc.getUuid().equals(newAcc.getUuid())) {
            addToBalance(oldAcc, oldVal.negate()); // estorna (receita somou; estorno subtrai)
            addToBalance(newAcc, newVal);          // aplica na nova
            accountsRepository.save(oldAcc);
            accountsRepository.save(newAcc);
        } else {
            addToBalance(oldAcc, newVal.subtract(oldVal)); // delta (novo - antigo)
            accountsRepository.save(oldAcc);
        }

        return recipheRepository.save(persisted);
    }

    @Transactional
    public void delete(UUID id) {
        User me = securityService.obterUserLogin();

        Reciphe entity = isAdmin(me)
                ? recipheRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : recipheRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        Accounts acc = entity.getAccounts();
        addToBalance(acc, bd(entity.getValue()).negate()); // estorna (retira)
        accountsRepository.save(acc);

        recipheRepository.delete(entity);
    }

    // TOGGLES
    @Transactional
    public void deactivate(UUID id) {
        Reciphe r = findById(id);
        if (r.getActive() == Active.DISABLE) return;
        Accounts acc = r.getAccounts();
        addToBalance(acc, bd(r.getValue()).negate()); // receita somou; desativar subtrai
        accountsRepository.save(acc);
        r.setActive(Active.DISABLE);
        recipheRepository.save(r);
    }

    @Transactional
    public void activate(UUID id) {
        Reciphe r = findById(id);
        if (r.getActive() == Active.ACTIVE) return;
        assertAccountActive(r.getAccounts());
        assertCategoryActive(r.getCategory());
        Accounts acc = r.getAccounts();
        addToBalance(acc, bd(r.getValue())); // aplica de novo
        accountsRepository.save(acc);
        r.setActive(Active.ACTIVE);
        recipheRepository.save(r);
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
