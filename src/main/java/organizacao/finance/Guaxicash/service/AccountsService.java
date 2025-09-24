package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountsService {

    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityService securityService;

    private boolean isAdmin(User me) {
        return me.getRole() == UserRole.ADMIN;
    }

    public List<Accounts> findAll() {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) {
            return accountsRepository.findAll();
        }
        return accountsRepository.findByUser(me);
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

    public Accounts insert(Accounts accounts) {
        UUID bankId = accounts.getBank().getUuid();
        UUID typeId = accounts.getType().getUuid();

        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundExeption(bankId));
        Type type = typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundExeption(typeId));

        User userAuth  = securityService.obterUserLogin();
        User user = userRepository.findById(userAuth.getUuid())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuário não encontrado no banco"));

        accounts.setUser(user);
        accounts.setBank(bank);
        accounts.setType(type);

        return accountsRepository.save(accounts);
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

    public Accounts update(UUID id, Accounts updatedAccount) {
        try {
            Accounts entity = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            User me = securityService.obterUserLogin();
            if (!isAdmin(me) && !entity.getUser().getUuid().equals(me.getUuid())) {
                throw new AccessDeniedException("Você não tem permissão para atualizar esta conta.");
            }

            updateData(entity, updatedAccount);
            return accountsRepository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundExeption(id);
        }
    }

    private void updateData(Accounts entity, Accounts updatedAccount) {
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
            entity.setType(type);
        }

    }
    public List<Accounts> findByUser(UUID userId) {
        User me = securityService.obterUserLogin();

        if (!isAdmin(me) && !me.getUuid().equals(userId)) {
            throw new AccessDeniedException("Você não tem permissão para consultar contas de outro usuário.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExeption(userId));

        return accountsRepository.findByUser(user);
    }


    public Double totalBalanceOfLogged() {
        User me = securityService.obterUserLogin();

        var stream = (isAdmin(me)
                ? accountsRepository.findAll().stream()
                : accountsRepository.findByUser(me).stream())
                .map(Accounts::getBalance)      // Stream<Double>
                .filter(Objects::nonNull);

        double sum = stream.mapToDouble(d -> d.doubleValue()).sum();

        return BigDecimal.valueOf(sum)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
