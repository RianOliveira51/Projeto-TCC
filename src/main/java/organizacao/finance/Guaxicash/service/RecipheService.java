package organizacao.finance.Guaxicash.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
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

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    public List<Reciphe> findAll() {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) return recipheRepository.findAll();
        return recipheRepository.findByAccounts_User(me); // ✅ caminho correto
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

        if (start == null && end == null) {
            throw new IllegalArgumentException("Informe ao menos uma data (from/to).");
        }
        if (start == null) start = end;
        if (end == null)   end   = start;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Parâmetro 'to' não pode ser anterior a 'from'.");
        }

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

        UUID categoryId = reciphe.getCategory().getUuid();
        UUID accountId  = reciphe.getAccounts().getUuid();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundExeption(categoryId));
        Accounts accounts = accountsRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundExeption(accountId));

        // segurança: dono da conta ou ADMIN
        User me = securityService.obterUserLogin();
        if (!isAdmin(me) && !accounts.getUser().getUuid().equals(me.getUuid())) {
            throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
        }

        reciphe.setCategory(category);
        reciphe.setAccounts(accounts);

        // soma no saldo com BigDecimal (2 casas)
        BigDecimal inc = BigDecimal.valueOf(reciphe.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cur = BigDecimal.valueOf(accounts.getBalance() == null ? 0.0 : accounts.getBalance())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal novo = cur.add(inc).setScale(2, RoundingMode.HALF_UP);
        accounts.setBalance(novo.floatValue());

        // salva tudo na mesma transação
        accountsRepository.save(accounts);
        return recipheRepository.save(reciphe);
    }

    // ===== UPDATE =====
    @Transactional
    public Reciphe update(UUID id, Reciphe payload) {
        User me = securityService.obterUserLogin();

        // carrega com escopo de dono
        Reciphe persisted = isAdmin(me)
                ? recipheRepository.findById(id).orElseThrow(() -> new ResourceNotFoundExeption(id))
                : recipheRepository.findByUuidAndAccounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        Accounts oldAcc = persisted.getAccounts();
        BigDecimal oldVal = bd(persisted.getValue());

        // Troca de conta (se veio no payload)
        if (payload.getAccounts() != null && payload.getAccounts().getUuid() != null) {
            Accounts newAcc = accountsRepository.findById(payload.getAccounts().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getAccounts().getUuid()));
            if (!isAdmin(me) && !newAcc.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Conta não pertence ao usuário autenticado.");
            }
            persisted.setAccounts(newAcc);
        }

        if (payload.getCategory() != null && payload.getCategory().getUuid() != null) {
            Category cat = categoryRepository.findById(payload.getCategory().getUuid())
                    .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));
            persisted.setCategory(cat);
        }

        // Atualiza campos simples (parcial)
        if (payload.getDescription() != null)        persisted.setDescription(payload.getDescription());
        if (payload.getDateRegistration() != null)   persisted.setDateRegistration(payload.getDateRegistration());
        if (payload.getValue() != null)              persisted.setValue(payload.getValue());

        // valida novo valor (> 0)
        BigDecimal newVal = bd(persisted.getValue());
        if (newVal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do recebimento deve ser maior que zero.");
        }

        Accounts newAcc = persisted.getAccounts();

        if (!oldAcc.getUuid().equals(newAcc.getUuid())) {
            // mudou de conta: estorna na antiga e soma na nova
            addToBalance(oldAcc, oldVal.negate());
            addToBalance(newAcc, newVal);
            accountsRepository.save(oldAcc);
            accountsRepository.save(newAcc);
        } else {
            // mesma conta: aplica apenas o delta (novo - antigo)
            addToBalance(oldAcc, newVal.subtract(oldVal));
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
        addToBalance(acc, bd(entity.getValue()).negate()); // estorna
        accountsRepository.save(acc);

        recipheRepository.delete(entity);
    }

    // ===== Helpers =====
    private static BigDecimal bd(Float v) {
        double d = (v == null ? 0.0 : v.doubleValue());
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }

    private void addToBalance(Accounts acc, BigDecimal delta) {
        BigDecimal cur = BigDecimal.valueOf(acc.getBalance() == null ? 0.0 : acc.getBalance())
                .setScale(2, RoundingMode.HALF_UP);
        acc.setBalance(cur.add(delta).setScale(2, RoundingMode.HALF_UP).floatValue());
    }


}
