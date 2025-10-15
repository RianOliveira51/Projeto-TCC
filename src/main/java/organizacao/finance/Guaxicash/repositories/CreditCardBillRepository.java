package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, UUID> {

    List<CreditCardBill> findByCreditCard_Accounts_User(User user);
    Optional<CreditCardBill> findByUuidAndCreditCard_Accounts_User_Uuid(UUID id, UUID userId);

    List<CreditCardBill> findByCreditCard_Accounts_User_UuidAndRegistrationDateBetween(
            UUID userId, LocalDate start, LocalDate end, Sort sort);

    List<CreditCardBill> findByCreditCard_UuidAndCreditCard_Accounts_User_UuidAndRegistrationDateBetween(
            UUID creditCardId, UUID userId, LocalDate start, LocalDate end, Sort sort);

    List<CreditCardBill> findByCreditCard_Accounts_User_UuidAndBill_PayDateBetween(
            UUID userId, LocalDate start, LocalDate end, Sort sort);

    List<CreditCardBill> findByCreditCard_UuidAndCreditCard_Accounts_User_UuidAndBill_PayDateBetween(
            UUID creditCardId, UUID userId, LocalDate start, LocalDate end, Sort sort);

    // ==== novos filtros por Active ====
    List<CreditCardBill> findByCreditCard_Accounts_UserAndActive(User user, Active active);
    Optional<CreditCardBill> findByUuidAndActive(UUID id, Active active);

    // ADMIN: por bill, sem user
    List<CreditCardBill> findByBill_Uuid(UUID billId, Sort sort);
    List<CreditCardBill> findByBill_UuidAndActive(UUID billId, Active active, Sort sort);


    // USER: por bill, filtrando pelo dono
    List<CreditCardBill> findByBill_UuidAndCreditCard_Accounts_User_Uuid(
            UUID billId, UUID userId, Sort sort);

    List<CreditCardBill> findByBill_UuidAndCreditCard_Accounts_User_UuidAndActive(
            UUID billId, UUID userId, Active active, Sort sort);

    @Query("""
        select count(c)
          from CreditCardBill c
         where c.creditCard.accounts.user.uuid = :userId
           and c.registrationDate between :start and :end
    """)
    long countByUserBetween(UUID userId, LocalDate start, LocalDate end);

    @Modifying
    @Transactional
    void deleteByCreditCard_Accounts_User_Uuid(UUID userId);
}
