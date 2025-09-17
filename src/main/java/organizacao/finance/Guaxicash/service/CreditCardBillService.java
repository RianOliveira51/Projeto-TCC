package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CreditCardBillService {
    @Autowired
    private CreditCardBillRepository creditCardBillRepository;
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private BillService billService;
    @Autowired
    private CategoryRepository categoryRepository;
   @Autowired
    private SecurityService securityService;

    private boolean isAdmin(User me) {
        return me.getRole() == UserRole.ADMIN;
    }

    public List<CreditCardBill> findAll() {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) {
            return creditCardBillRepository.findAll();
        }
        return creditCardBillRepository.findByCreditCard_Accounts_User(me);
    }

    public CreditCardBill findById(UUID id) {
        User me = securityService.obterUserLogin();
        if (isAdmin(me)) {
            return creditCardBillRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundExeption(id));
        }
        return creditCardBillRepository.findByUuidAndCreditCard_Accounts_User_Uuid(id, me.getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
    }

    public List<CreditCardBill> searchByDate(String field, LocalDate from, LocalDate to, UUID creditCardId) {
        if (from == null && to == null) throw new IllegalArgumentException("Informe ao menos uma data.");
        if (from == null) from = to;
        if (to == null)   to   = from;
        if (to.isBefore(from)) throw new IllegalArgumentException("'to' não pode ser anterior a 'from'.");

        var me = securityService.obterUserLogin();
        boolean byRegistration = (field == null) || field.equalsIgnoreCase("registration");
        Sort sort = byRegistration ? Sort.by("registrationDate").ascending()
                : Sort.by("bill.payDate").ascending();

        if (byRegistration) {
            return (creditCardId == null)
                    ? creditCardBillRepository.findByCreditCard_Accounts_User_UuidAndRegistrationDateBetween(me.getUuid(), from, to, sort)
                    : creditCardBillRepository.findByCreditCard_UuidAndCreditCard_Accounts_User_UuidAndRegistrationDateBetween(creditCardId, me.getUuid(), from, to, sort);
        } else {
            return (creditCardId == null)
                    ? creditCardBillRepository.findByCreditCard_Accounts_User_UuidAndBill_PayDateBetween(me.getUuid(), from, to, sort)
                    : creditCardBillRepository.findByCreditCard_UuidAndCreditCard_Accounts_User_UuidAndBill_PayDateBetween(creditCardId, me.getUuid(), from, to, sort);
        }
    }

    @Transactional
    public CreditCardBill insert(CreditCardBill creditCardBill) {
        // Carregar associações
        UUID creditCardId = creditCardBill.getCreditCard().getUuid();
        UUID billId       = creditCardBill.getBill().getUuid();
        UUID categoryId   = creditCardBill.getCategory().getUuid();

        CreditCard creditCard = creditCardRepository.findById(creditCardId)
                .orElseThrow(() -> new ResourceNotFoundExeption(creditCardId));
        Bill initialBill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundExeption(categoryId));

        creditCardBill.setCreditCard(creditCard);
        creditCardBill.setBill(initialBill);
        creditCardBill.setCategory(category);

        // Valor total da compra
        BigDecimal total = BigDecimal.valueOf(creditCardBill.getValue() == null ? 0.0 : creditCardBill.getValue())
                .setScale(2, RoundingMode.HALF_UP);

        // Parcelado?
        boolean isInstallments = creditCardBill.getInstallments() != null
                && creditCardBill.getInstallments().trim().equalsIgnoreCase("sim");
        Integer n = creditCardBill.getNumberinstallments();

        if (!isInstallments || n == null || n <= 1) {
            // Sem parcelas -> soma na fatura informada
            addToBillValue(initialBill, total);
            billRepository.save(initialBill);
            return creditCardBillRepository.save(creditCardBill);
        }

        // Parcelado: garantir faturas futuras geradas até 12/2025
        billService.generateBillsUntilDec2025(creditCard);

        // Dividir valor igualmente
        BigDecimal per = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acc = BigDecimal.ZERO;
        List<Bill> changed = new ArrayList<>();

        YearMonth baseYm = YearMonth.from(initialBill.getCloseDate());
        for (int i = 0; i < n; i++) {
            YearMonth ym = baseYm.plusMonths(i);
            Bill target = findBillByMonthOrThrow(creditCard, ym);

            BigDecimal amount = (i < n - 1) ? per : total.subtract(acc).setScale(2, RoundingMode.HALF_UP);
            acc = acc.add(amount);

            addToBillValue(target, amount);
            changed.add(target);
        }

        if (!changed.isEmpty()) billRepository.saveAll(changed);
        return creditCardBillRepository.save(creditCardBill);
    }

    // Soma com null-safety (Bill.value é Double)
    private void addToBillValue(Bill bill, BigDecimal inc) {
        double current = bill.getValue() == null ? 0.0 : bill.getValue();
        bill.setValue(BigDecimal.valueOf(current).add(inc).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    // Localiza a fatura do mês (pelo closeDate do mês)
    private Bill findBillByMonthOrThrow(CreditCard card, YearMonth ym) {
        return billRepository.findFirstByCreditCardAndCloseDateBetween(
                        card, ym.atDay(1), ym.atEndOfMonth()
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fatura do cartão %s não encontrada para %s".formatted(
                                card.getUuid(), ym
                        )
                ));
    }

    @Transactional
    public CreditCardBill update(UUID id, CreditCardBill payload) {
        // carrega atual
        CreditCardBill persisted = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        // 1) estorna efeito antigo
        adjustBills(persisted, -1);

        // 2) reata associações (PUT completo)
        var cc  = creditCardRepository.findById(payload.getCreditCard().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCreditCard().getUuid()));
        var bil = billRepository.findById(payload.getBill().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getBill().getUuid()));
        var cat = categoryRepository.findById(payload.getCategory().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));

        persisted.setCreditCard(cc);
        persisted.setBill(bil);
        persisted.setCategory(cat);
        persisted.setValue(payload.getValue());
        persisted.setDescription(payload.getDescription());
        persisted.setRegistrationDate(payload.getRegistrationDate());
        persisted.setInstallments(payload.getInstallments());
        persisted.setNumberinstallments(payload.getNumberinstallments());

        // 3) reaplica efeito novo
        adjustBills(persisted, +1);

        return creditCardBillRepository.save(persisted);
    }

    @Transactional
    public void delete(UUID id) {
        var entity = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));
        // estorna e apaga
        adjustBills(entity, -1);
        creditCardBillRepository.delete(entity);
    }

    //compacto: aplica (sign=+1) ou estorna (sign=-1) nas faturas
    private void adjustBills(CreditCardBill ccb, int sign) {
        billService.generateBillsUntilDec2025(ccb.getCreditCard()); // garante faturas futuras

        BigDecimal total = BigDecimal.valueOf(ccb.getValue() == null ? 0.0 : ccb.getValue())
                .setScale(2, RoundingMode.HALF_UP);

        // installments =m("sim" / "nao")
        boolean parcelado = "sim".equalsIgnoreCase(
                java.util.Objects.toString(ccb.getInstallments(), "").trim()
        );

        int n = parcelado ? Math.max(1, ccb.getNumberinstallments()) : 1;

        java.time.YearMonth base = java.time.YearMonth.from(ccb.getBill().getCloseDate());
        BigDecimal per = (n == 1) ? total : total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acc = BigDecimal.ZERO;

        java.util.List<Bill> changed = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            java.time.YearMonth ym = base.plusMonths(i);
            Bill b = billRepository.findFirstByCreditCardAndCloseDateBetween(
                    ccb.getCreditCard(), ym.atDay(1), ym.atEndOfMonth()
            ).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Fatura não encontrada para " + ym));

            BigDecimal amt = (i < n - 1) ? per : total.subtract(acc).setScale(2, RoundingMode.HALF_UP);
            acc = acc.add(amt);

            double cur = b.getValue() == null ? 0.0 : b.getValue();
            b.setValue(BigDecimal.valueOf(cur).add(amt.multiply(BigDecimal.valueOf(sign)))
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
            changed.add(b);
        }
        if (!changed.isEmpty()) billRepository.saveAll(changed);
    }


}
