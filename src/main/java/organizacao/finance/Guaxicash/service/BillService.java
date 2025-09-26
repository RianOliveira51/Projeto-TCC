package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

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

        LocalDate today = LocalDate.now();
        YearMonth startYm = YearMonth.from(today);
        YearMonth endYm   = YearMonth.of(2025, 12);

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
                bill.setValuepay(0.0);
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

    //Atualizar
    public void rescheduleFutureBills(CreditCard card) {
        LocalDate today = LocalDate.now(ZONE);

        List<Bill> bills = billRepository.findByCreditCardAndStatusAndPayDateGreaterThanEqual(
                card, BillPay.FUTURE_BILLS, today
        );

        int newCloseDay  = card.getCloseDate().getDayOfMonth();
        int newDueDay    = card.getExpiryDate().getDayOfMonth();

        for (Bill b : bills) {
            // O "mês da fatura" continua sendo o mesmo do fechamento atual
            YearMonth ym = YearMonth.from(b.getCloseDate());

            // Novo fechamento no mesmo mês da fatura, ajustando ao tamanho do mês
            LocalDate newCloseDate = atSafeDay(ym, newCloseDay);

            // Novo vencimento no mês seguinte
            YearMonth dueYm = ym.plusMonths(1);
            LocalDate newPayDate = atSafeDay(dueYm, newDueDay);

            // Novo "fechamento" do mês anterior para calcular a abertura
            YearMonth prevYm = ym.minusMonths(1);
            LocalDate prevClose = atSafeDay(prevYm, newCloseDay);
            LocalDate newOpenDate = prevClose.plusDays(1);

            // Por segurança: open <= close e pay >= close
            if (newOpenDate.isAfter(newCloseDate)) {
                // fallback: início do próprio mês (raro, se fechamento for dia 1)
                newOpenDate = ym.atDay(1);
            }
            if (newPayDate.isBefore(newCloseDate)) {
                // fallback: empurra o vencimento para o mesmo mês do fechamento (caso extremo)
                newPayDate = newCloseDate;
            }

            b.setOpenDate(newOpenDate);
            b.setCloseDate(newCloseDate);
            b.setPayDate(newPayDate);
        }

        billRepository.saveAll(bills);
    }

    @Transactional
    public Bill registerPayment(UUID billId, Double amount) {
        if (amount == null) throw new IllegalArgumentException("Informe o valor do pagamento.");
        BigDecimal inc = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        if (inc.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O pagamento deve ser maior que zero.");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));

        BigDecimal total   = BigDecimal.valueOf(bill.getValue()    == null ? 0.0 : bill.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal current = BigDecimal.valueOf(bill.getValuepay() == null ? 0.0 : bill.getValuepay()).setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Fatura sem valor total definido.");
        }

        BigDecimal remaining = total.subtract(current); // quanto falta pagar
        if (inc.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Pagamento excede o valor restante da fatura. Falta pagar: " + remaining);
        }

        BigDecimal newPaid = current.add(inc).setScale(2, RoundingMode.HALF_UP);
        bill.setValuepay(newPaid.doubleValue());

        // Se quitou, marca como paga
        if (newPaid.compareTo(total) == 0) {
            bill.setStatus(BillPay.PAID);
        }
        return billRepository.save(bill);
    }

}
