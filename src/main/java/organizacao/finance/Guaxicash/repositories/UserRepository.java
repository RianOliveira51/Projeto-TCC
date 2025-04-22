package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import organizacao.finance.Guaxicash.entities.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

     UserDetails findByEmail(String email);
}
