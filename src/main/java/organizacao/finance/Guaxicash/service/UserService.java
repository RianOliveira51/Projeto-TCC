package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.UserRole;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    public List<User> findall(){ return userRepository.findAll(); }

    public User findById(UUID id){
        Optional<User> obj = userRepository.findById(id);
        return obj.orElseThrow(()-> new ResourceNotFoundExeption(id));
    }

    public User insert(User user){
        return userRepository.save(user);
    }

    public void delete(UUID id) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        if (!accountsRepository.findByUser(targetUser).isEmpty()) {

            throw new IllegalStateException("Usuário possui contas associadas e não pode ser excluído.");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();

        boolean isAdmin = authUser.getRole().equals(UserRole.ADMIN);
        boolean isOwner = authUser.getUuid().equals(targetUser.getUuid());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Você não tem permissão para deletar este usuário.");
        }

        userRepository.delete(targetUser);
    }

    public User update(UUID id, User user){
        try{
            User entity = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User authUser = (User) auth.getPrincipal();

            boolean isAdmin = authUser.getRole().equals(UserRole.ADMIN);
            boolean isOwner = authUser.getUuid().equals(entity.getUuid());

            if (!isAdmin && !isOwner) {
                throw new SecurityException("Você não tem permissão para atualizar este usuário.");
            }

            updateData(entity, user);
            return userRepository.save(entity);
        }catch (EntityNotFoundException e){
            throw new ResourceNotFoundExeption(id);
        }
    }

    public void updateData( User entity, User user){
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setPassword(user.getPassword());
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email);
    }

}
