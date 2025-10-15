package organizacao.finance.Guaxicash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.service.EventGamification.GamificationEventPublisher;
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

    @Autowired private BillRepository billRepository;
    @Autowired private GamificationEventPublisher gamificationEventPublisher;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    public List<Bill> findByUserId(UUID userId) {
        return billRepository.findByCreditCardAccountsUserUuid(userId);
    }
    public List<Bill> findByUserId(UUID userId, Active active) {
        return billRepository.findByCreditCardAccountsUserUuidAndActive(userId, active);
    }
    public List<Bill> findByCreditCard(UUID cardId) {
        return billRepository.findByCreditCardUuid(cardId);
    }
    public List<Bill> findByCreditCard(UUID cardId, Active active) {
        return billRepository.findByCreditCardUuidAndActive(cardId, active);
    }

    public Bill insert(Bill bill) {
        if (bill.getActive() == null) bill.setActive(Active.ACTIVE);
        if (bill.getActive() != Active.ACTIVE) {
            throw new IllegalArgumentException("Não é possível criar fatura desativada.");
        }
        return billRepository.save(bill);
    }

    /**
     * Helper idempotente: pega a fatura do mês (cartão + closeDate do mês),
     * ou cria se não existir.
     */
    @Transactional
    public Bill getOrCreateBillForMonth(CreditCard card, YearMonth ym) {
        LocalDate close = atSafeDay(ym, card.getCloseDate().getDayOfMonth());
        return billRepository.findByCreditCardAndCloseDate(card, close)
                .orElseGet(() -> {
                    // calcula open e pay
                    LocalDate prevClosing = atSafeDay(ym.minusMonths(1), card.getCloseDate().getDayOfMonth());
                    LocalDate open = prevClosing.plusDays(1);
                    int dueDay = card.getExpiryDate().getDayOfMonth();
                    LocalDate due = (dueDay > close.getDayOfMonth())
                            ? atSafeDay(ym, dueDay)
                            : atSafeDay(ym.plusMonths(1), dueDay);

                    LocalDate today = LocalDate.now();
                    Bill b = new Bill();
                    b.setCreditCard(card);
                    b.setOpenDate(open);
                    b.setCloseDate(close);
                    b.setPayDate(due);
                    b.setStatus((!today.isBefore(open) && !today.isAfter(close))
                            ? BillPay.OPEN
                            : BillPay.FUTURE_BILLS);
                    b.setValue(0.0);
                    b.setValuepay(0.0);
                    b.setActive(Active.ACTIVE);
                    return billRepository.save(b);
                });
    }

    @Transactional
    public void generateBillsUntilDec2025(CreditCard card) {
        LocalDate today = LocalDate.now();
        YearMonth startYm = YearMonth.from(today);
        YearMonth endYm   = YearMonth.of(2025, 12);

        List<Bill> toSave = new ArrayList<>();

        int closeDay = card.getCloseDate().getDayOfMonth();
        int dueDay   = card.getExpiryDate().getDayOfMonth();

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            LocalDate closing = atSafeDay(ym, closeDay);
            if (closing.isBefore(today)) continue;

            LocalDate prevClosing = atSafeDay(ym.minusMonths(1), closeDay);
            LocalDate opening = prevClosing.plusDays(1);

            LocalDate due = (dueDay > closeDay) ? atSafeDay(ym, dueDay)
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
                bill.setActive(Active.ACTIVE);
                toSave.add(bill);
            }
        }
        if (!toSave.isEmpty()) billRepository.saveAll(toSave);
    }

    static LocalDate atSafeDay(YearMonth ym, int day) {
        int safe = Math.min(day, ym.lengthOfMonth());
        return ym.atDay(safe);
    }

    public void rescheduleFutureBills(CreditCard card) {
        LocalDate today = LocalDate.now(ZONE);

        List<Bill> bills = billRepository.findByCreditCardAndStatusAndPayDateGreaterThanEqual(
                card, BillPay.FUTURE_BILLS, today
        );

        int newCloseDay  = card.getCloseDate().getDayOfMonth();
        int newDueDay    = card.getExpiryDate().getDayOfMonth();

        for (Bill b : bills) {
            YearMonth ym = YearMonth.from(b.getCloseDate());
            LocalDate newCloseDate = atSafeDay(ym, newCloseDay);
            YearMonth dueYm = ym.plusMonths(1);
            LocalDate newPayDate = atSafeDay(dueYm, newDueDay);
            YearMonth prevYm = ym.minusMonths(1);
            LocalDate prevClose = atSafeDay(prevYm, newCloseDay);
            LocalDate newOpenDate = prevClose.plusDays(1);
            if (newOpenDate.isAfter(newCloseDate)) newOpenDate = ym.atDay(1);
            if (newPayDate.isBefore(newCloseDate)) newPayDate = newCloseDate;
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
        if (inc.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("O pagamento deve ser maior que zero.");

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));

        BigDecimal total   = BigDecimal.valueOf(bill.getValue() == null ? 0.0 : bill.getValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal current = BigDecimal.valueOf(bill.getValuepay() == null ? 0.0 : bill.getValuepay()).setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalStateException("Fatura sem valor total definido.");

        BigDecimal remaining = total.subtract(current);
        if (inc.compareTo(remaining) > 0) throw new IllegalArgumentException("Pagamento excede o restante. Falta pagar: " + remaining);

        BigDecimal newPaid = current.add(inc).setScale(2, RoundingMode.HALF_UP);
        bill.setValuepay(newPaid.doubleValue());
        if (newPaid.compareTo(total) == 0) bill.setStatus(BillPay.PAID);

        if (newPaid.compareTo(total) == 0) {
            bill.setStatus(BillPay.PAID);

            boolean beforeDue = LocalDate.now().isBefore(bill.getPayDate()) || LocalDate.now().isEqual(bill.getPayDate());
            YearMonth cycle = YearMonth.from(bill.getCloseDate());
            gamificationEventPublisher.billPaid(
                    bill.getCreditCard().getAccounts().getUser().getUuid(),
                    bill.getUuid(),
                    cycle,
                    beforeDue
            );
        }

        return billRepository.save(bill);
    }

    @Transactional
    public Bill setValue(UUID billId, Double newValue) {
        if (newValue == null) throw new IllegalArgumentException("Valor não pode ser nulo.");
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));
        assertNotClosed(bill);
        bill.setValue(newValue);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill addToValue(UUID billId, Double delta) {
        if (delta == null) throw new IllegalArgumentException("Delta não pode ser nulo.");
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));
        assertNotClosed(bill);
        double atual = bill.getValue() == null ? 0.0 : bill.getValue();
        bill.setValue(atual + delta);
        return billRepository.save(bill);
    }

    private void assertNotClosed(Bill bill) {
        if (bill.getStatus() == BillPay.CLOSE_PENDING) {
            throw new IllegalStateException("Fatura fechada (CLOSE) não pode alterar o valor.");
        }
    }

    @Transactional
    public void deactivate(UUID id) {
        Bill b = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        b.setActive(Active.DISABLE);
        billRepository.save(b);
    }

    @Transactional
    public void activate(UUID id) {
        Bill b = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        b.setActive(Active.ACTIVE);
        billRepository.save(b);
    }
}
