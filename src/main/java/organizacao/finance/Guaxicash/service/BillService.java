package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.BillRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public List<Bill> findByUserId(UUID userId) {
        return billRepository.findByCreditCardAccountsUserUuid(userId);
    }
    public List<Bill> findByCreditCard(UUID cardId) {
        return billRepository.findByCreditCardUuid(cardId);
    }

    public Bill insert(Bill bill) {
        return billRepository.save(bill);
    }

    @Transactional
    public void generateBillsUntilDec2025(CreditCard card) {
        // Datas base
        LocalDate today = LocalDate.now();
        YearMonth startYm = YearMonth.from(today);     // começa no mês atual
        YearMonth endYm   = YearMonth.of(2025, 12);    // vai até dezembro/2025

        // Dias relevantes (apenas o dia do mês interessa)
        int closeDay = card.getCloseDate().getDayOfMonth();   // dia de fechamento
        int dueDay   = card.getExpiryDate().getDayOfMonth();  // dia de vencimento

        List<Bill> toSave = new ArrayList<>();

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            // Data de FECHAMENTO desta fatura
            LocalDate closing = atSafeDay(ym, closeDay);

            // Pula faturas cujo fechamento já passou
            if (closing.isBefore(today)) continue;

            // Data de ABERTURA: (fechamento do mês anterior) + 1 dia
            LocalDate prevClosing = atSafeDay(ym.minusMonths(1), closeDay);
            LocalDate opening = prevClosing.plusDays(1);

            // Data de VENCIMENTO: se dueDay > closeDay, vence no mesmo mês; senão, no próximo
            LocalDate due = (dueDay > closeDay)
                    ? atSafeDay(ym, dueDay)
                    : atSafeDay(ym.plusMonths(1), dueDay);

            if (!billRepository.existsByCreditCardAndCloseDate(card, closing)) {
                Bill bill = new Bill();
                bill.setCreditCard(card);
                bill.setOpenDate(opening);
                bill.setCloseDate(closing);
                bill.setPayDate(due);
                BillPay status = (!today.isBefore(opening) && !today.isAfter(closing))
                        ? BillPay.OPEN
                        : BillPay.FUTURE_BILLS;
                bill.setStatus(status);
                bill.setValue(0.0);
                toSave.add(bill);
            }
        }

        if (!toSave.isEmpty()) {
            billRepository.saveAll(toSave);
        }
    }

    private static LocalDate atSafeDay(YearMonth ym, int day) {
        int safe = Math.min(day, ym.lengthOfMonth());
        return ym.atDay(safe);

    }
}
