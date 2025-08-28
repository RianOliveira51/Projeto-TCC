package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {

    // Lista todos os cartões do usuário (dono)
    List<CreditCard> findAllByAccounts_User(User user);

    // Busca 1 cartão garantindo que pertence ao usuário
    Optional<CreditCard> findByUuidAndAccounts_User(UUID id, User user);

    // Verificação de propriedade (dono) — útil para update/delete
    boolean existsByUuidAndAccounts_User(UUID id, User user);

}
