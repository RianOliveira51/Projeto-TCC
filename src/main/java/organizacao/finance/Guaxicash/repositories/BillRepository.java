package organizacao.finance.Guaxicash.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {

    boolean existsByCreditCardAndCloseDate(CreditCard creditCard, LocalDate closeDate);

    List<Bill> findByCreditCardUuid(UUID creditCardId);

    List<Bill> findByCreditCardAccountsUserUuid(UUID userId);

    long deleteByCreditCardUuid(UUID creditCardId);

    // Faturas do cartão com status específico e vencimento a partir de uma data
    List<Bill> findByCreditCardAndStatusAndPayDateGreaterThanEqual(
            CreditCard creditCard, BillPay status, LocalDate from);

    // (Opcional) mesmas consultas com ordenação
    List<Bill> findByStatus(BillPay status, Sort sort);
    List<Bill> findByCreditCard_UuidAndStatus(UUID creditCardId, BillPay status, Sort sort);
    List<Bill> findByStatusIn(List<BillPay> statuses, Sort sort);
}
