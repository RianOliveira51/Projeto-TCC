package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;

import java.util.List;
import java.util.UUID;

public interface AccountsRepository extends JpaRepository<Accounts, UUID> {
    List<Accounts> findByUser(User user);

    // filtros por active
    List<Accounts> findAllByActive(Active active);
    List<Accounts> findByUserAndActive(User user, Active active);

    @Query("""
   select count(a) from Accounts a
    where a.user.uuid = :userId
      and a.active = organizacao.finance.Guaxicash.entities.Enums.Active.ACTIVE
""")
    long countActiveByUser(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    void deleteByUser_Uuid(UUID userId);
}
