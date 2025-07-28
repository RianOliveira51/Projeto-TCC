package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
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

    public List<Accounts> findAll() {return accountsRepository.findAll();
    }

    public Accounts findById(UUID id) {
        Optional<Accounts> accounts = accountsRepository.findById(id);
        return accounts.orElseThrow(() -> new ResourceNotFoundExeption(id));
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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado no banco"));

        accounts.setUser(user);
        accounts.setBank(bank);
        accounts.setType(type);

        return accountsRepository.save(accounts);
    }
    public List<Accounts> findByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExeption(userId));

        return accountsRepository.findByUser(user);
    }

    public void delete(UUID id){
        try {
            Accounts entity = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            // Verifica se a conta pertence ao usuário logado
            User authUser = securityService.obterUserLogin();
            if (!entity.getUser().getUuid().equals(authUser.getUuid())) {
                throw new SecurityException("Você não tem permissão para deletar esta conta.");
            }

        }catch (ResourceNotFoundExeption e){
            throw new ResourceNotFoundExeption(id);
        }catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }
    public Accounts update(UUID id, Accounts updatedAccount) {
        try {
            Accounts entity = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
            //Verifica se a conta pertence ao usuário logado
            User authUser = securityService.obterUserLogin();
            if (!entity.getUser().getUuid().equals(authUser.getUuid())) {
                throw new SecurityException("Você não tem permissão para atualizar esta conta.");
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
        entity.setType(updatedAccount.getType());
        entity.setBank(updatedAccount.getBank());

        if (updatedAccount.getBank() != null) {
            entity.setBank(updatedAccount.getBank());
        }

        if (updatedAccount.getType() != null) {
            entity.setType(updatedAccount.getType());
        }

        if(updatedAccount.getUser() != null) {
            entity.setUser(updatedAccount.getUser());
        }
    }
}
