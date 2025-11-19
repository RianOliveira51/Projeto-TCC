package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Expenses;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expenses, UUID> {

    List<Expenses> findByAccounts_User(User user);
    Optional<Expenses> findByUuidAndAccounts_User_Uuid(UUID id, UUID userId);

    List<Expenses> findByAccounts_User_UuidAndDateRegistrationBetween(
            UUID userId, LocalDate from, LocalDate to, Sort sort
    );

    // Filtros por status
    List<Expenses> findAllByActive(Active active);
    List<Expenses> findByAccounts_UserAndActive(User user, Active active);

    // (opcional) busca por data + active, se quiser usar no futuro
    List<Expenses> findByAccounts_User_UuidAndActiveAndDateRegistrationBetween(
            UUID userId, Active active, LocalDate from, LocalDate to, Sort sort
    );
    List<Expenses> findByAccountsAndActive(Accounts accounts, Active active);

    long countByAccounts_User_Uuid(UUID userId);

    // despesas em um dia específico (para missão 4)
    @Query("""
   select coalesce(count(e),0) from Expenses e
    where e.accounts.user.uuid = :userId
      and e.dateRegistration = :day
""")
    int countInDay(@Param("userId") UUID userId, @Param("day") LocalDate day);

    @Query("""
        select count(e)
          from Expenses e
         where e.accounts.user.uuid = :userId
           and e.dateRegistration between :start and :end
    """)
    long countBetween(UUID userId, LocalDate start, LocalDate end);

    // ExpenseRepository.java
    @Modifying
    @Transactional
    void deleteByAccounts_User_Uuid(UUID userId);

    // ExpenseRepository.java
    @Query("""
    select coalesce(sum(e.value), 0)
      from Expenses e
     where e.accounts.user.uuid = :userId
       and (:active is null or e.active = :active)
       and (:from  is null or e.dateRegistration >= :from)
       and (:to    is null or e.dateRegistration <= :to)
""")
    Double sumByUserWithFilters(@Param("userId") UUID userId,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to,
                                @Param("active") Active active);

}
