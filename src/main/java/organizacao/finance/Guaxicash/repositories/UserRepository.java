package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

     UserDetails findByEmail(String email);
     boolean existsByEmail(String email);

     List<User> findAllByActive(Active active);
     Optional<User> findByUuidAndActive(UUID uuid, Active active);

}
