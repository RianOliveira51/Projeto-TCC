package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private AccountsRepository accountsRepository;

    // ===== LISTAR =====
    public List<User> findall() { return userRepository.findAll(); }
    public List<User> findAllByActive(Active active) { return userRepository.findAllByActive(active); }

    public User findById(UUID id){
        Optional<User> obj = userRepository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public User findByIdAndActive(UUID id, Active active){
        return userRepository.findByUuidAndActive(id, active)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    // ===== CRIAR / ATUALIZAR =====
    public User insert(User user){
        if (user.getActive() == null) user.setActive(Active.ACTIVE);
        return userRepository.save(user);
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

    public void updateData(User entity, User user){
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setPassword(user.getPassword());
        // não expomos set de role/active aqui para evitar abuso por usuários comuns
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

    public void softDelete(UUID id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        boolean isAdmin = authUser.getRole().equals(UserRole.ADMIN);
        boolean isOwner = authUser.getUuid().equals(target.getUuid());

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Você não tem permissão para desativar este usuário.");
        }

        target.setActive(Active.DISABLE);
        userRepository.save(target);
    }

    public void enable(UUID id) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        // apenas admin reativa outros usuários
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        boolean isAdmin = authUser.getRole().equals(UserRole.ADMIN);
        if (!isAdmin) {
            throw new SecurityException("Apenas administradores podem reativar usuários.");
        }

        target.setActive(Active.ACTIVE);
        userRepository.save(target);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails ud = userRepository.findByEmail(email);
        if (ud == null) throw new UsernameNotFoundException("Usuário não encontrado");

        if (ud instanceof User u && u.getActive() == Active.DISABLE) {
            throw new DisabledException("Usuário desativado");
        }
        return ud;
    }


}
