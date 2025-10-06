package organizacao.finance.Guaxicash.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    boolean existsByCreditCardAndCloseDate(CreditCard creditCard, LocalDate closeDate);

    List<Bill> findByCreditCardUuid(UUID creditCardId);
    List<Bill> findByCreditCardAccountsUserUuid(UUID userId);

    long deleteByCreditCardUuid(UUID creditCardId);

    List<Bill> findByCreditCardAndStatusAndPayDateGreaterThanEqual(
            CreditCard creditCard, BillPay status, LocalDate from);

    List<Bill> findByStatus(BillPay status, Sort sort);
    List<Bill> findByCreditCard_UuidAndStatus(UUID creditCardId, BillPay status, Sort sort);
    List<Bill> findByStatusIn(List<BillPay> statuses, Sort sort);

    Optional<Bill> findFirstByCreditCardAndCloseDateBetween(
            CreditCard creditCard, LocalDate startInclusive, LocalDate endInclusive);

    // ==== novos para Active ====
    List<Bill> findByCreditCard(CreditCard card);
    List<Bill> findByCreditCardIn(List<CreditCard> cards);
    List<Bill> findByCreditCardInAndActive(List<CreditCard> cards, Active active);
    List<Bill> findByCreditCardUuidAndActive(UUID creditCardId, Active active);
    List<Bill> findByCreditCardAccountsUserUuidAndActive(UUID userId, Active active);
    List<Bill> findByStatusAndActive(BillPay status, Active active, Sort sort);
    List<Bill> findByCreditCard_UuidAndStatusAndActive(UUID creditCardId, BillPay status, Active active, Sort sort);
    List<Bill> findByStatusInAndActive(List<BillPay> statuses, Active active, Sort sort);

    List<Bill> findByActive(Active active, Sort sort);

    // ==== seus updates programados ====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update Bill b
          set b.status = :closeStatus
        where b.closeDate <= :today
          and b.status in (:eligible)
    """)
    int markBillsClosed(@Param("today") LocalDate today,
                        @Param("closeStatus") BillPay closeStatus,
                        @Param("eligible") List<BillPay> eligible);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update Bill b
          set b.status = :openStatus
        where b.openDate <= :today
          and b.closeDate >= :today
          and b.status = :futureStatus
    """)
    int markBillsOpenForToday(@Param("today") LocalDate today,
                              @Param("openStatus") BillPay openStatus,
                              @Param("futureStatus") BillPay futureStatus);
}
