package organizacao.finance.Guaxicash.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import organizacao.finance.Guaxicash.Config.SecurityService;
import organizacao.finance.Guaxicash.entities.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.UserRole;
import organizacao.finance.Guaxicash.repositories.*;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CreditCardBillService {

    @Autowired private CreditCardBillRepository creditCardBillRepository;
    @Autowired private CreditCardRepository creditCardRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private BillService billService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SecurityService securityService;

    private boolean isAdmin(User me) { return me.getRole() == UserRole.ADMIN; }

    private void assertActiveForPosting(CreditCard card, Bill bill) {
        if (card.getAccounts().getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Conta desativada. Não é possível lançar compras.");
        }
        if (card.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Cartão desativado. Não é possível lançar compras.");
        }
        if (bill.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Fatura desativada. Não é possível lançar compras.");
        }
    }

    private void assertCategoryActive(Category category) {
        if (category.getActive() != Active.ACTIVE) {
            throw new IllegalStateException("Categoria desativada. Não é possível lançar compras.");
        }
    }

    // ================== READ ==================
    public List<CreditCardBill> findAll() {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? creditCardBillRepository.findAll()
                : creditCardBillRepository.findByCreditCard_Accounts_User(me);
    }

    public List<CreditCardBill> findAll(Active active) {
        User me = securityService.obterUserLogin();
        return isAdmin(me) ? creditCardBillRepository.findAll().stream()
                .filter(b -> b.getActive() == active).toList()
                : creditCardBillRepository.findByCreditCard_Accounts_UserAndActive(me, active);
    }

    public List<CreditCardBill> findAllByBill(UUID billId, Active active) {
        User me = securityService.obterUserLogin();
        Sort sort = Sort.by("registrationDate").ascending();

        if (isAdmin(me)) {
            billRepository.findById(billId).orElseThrow(() -> new ResourceNotFoundExeption(billId));
            return (active == null)
                    ? creditCardBillRepository.findByBill_Uuid(billId, sort)
                    : creditCardBillRepository.findByBill_UuidAndActive(billId, active, sort);
        }

        UUID userId = me.getUuid();
        return (active == null)
                ? creditCardBillRepository.findByBill_UuidAndCreditCard_Accounts_User_Uuid(billId, userId, sort)
                : creditCardBillRepository.findByBill_UuidAndCreditCard_Accounts_User_UuidAndActive(billId, userId, active, sort);
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
        if (to == null) to = from;
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

    // ================== CREATE ==================
    /**
     * Se não for parcelado, cria um único item na fatura informada.
     * Se for parcelado, cria N itens — um para cada fatura futura —
     * e ajusta o valor de cada fatura correspondente.
     */
    @Transactional
    public CreditCardBill insert(CreditCardBill creditCardBill) {
        // Associações obrigatórias
        UUID creditCardId = creditCardBill.getCreditCard().getUuid();
        UUID billId       = creditCardBill.getBill().getUuid();
        UUID categoryId   = creditCardBill.getCategory().getUuid();

        CreditCard creditCard = creditCardRepository.findById(creditCardId)
                .orElseThrow(() -> new ResourceNotFoundExeption(creditCardId));
        Bill initialBill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundExeption(billId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundExeption(categoryId));

        // segurança: dono do cartão
        User me = securityService.obterUserLogin();
        boolean owner = creditCard.getAccounts().getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para lançar nesta fatura/cartão.");

        creditCardBill.setCreditCard(creditCard);
        creditCardBill.setBill(initialBill);
        creditCardBill.setCategory(category);
        if (creditCardBill.getActive() == null) creditCardBill.setActive(Active.ACTIVE);
        if (creditCardBill.getActive() == Active.DISABLE) {
            throw new IllegalArgumentException("Não é possível criar lançamento já desativado.");
        }
        creditCardBill.setActive(Active.ACTIVE);

        assertActiveForPosting(creditCard, initialBill);
        assertCategoryActive(category);

        // Valor total
        BigDecimal total = BigDecimal.valueOf(creditCardBill.getValue() == null ? 0.0 : creditCardBill.getValue())
                .setScale(2, RoundingMode.HALF_UP);

        // Parcelas?
        boolean isInstallments = creditCardBill.getInstallments() != null
                && creditCardBill.getInstallments().trim().equalsIgnoreCase("sim");
        Integer n = creditCardBill.getNumberinstallments();

        // Caso NÃO parcelado: adiciona um item na fatura atual
        if (!isInstallments || n == null || n <= 1) {
            addToBillValue(initialBill, total);
            billRepository.save(initialBill);
            return creditCardBillRepository.save(creditCardBill);
        }

        // Caso parcelado: cria um item por parcela na fatura correta
        billService.generateBillsUntilDec2025(creditCard);

        BigDecimal per = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal acc = BigDecimal.ZERO;
        List<Bill> changed = new ArrayList<>();
        List<CreditCardBill> itensParcelas = new ArrayList<>();

        YearMonth baseYm = YearMonth.from(initialBill.getCloseDate());
        for (int i = 0; i < n; i++) {
            YearMonth ym = baseYm.plusMonths(i);
            Bill target = findBillByMonthOrThrow(creditCard, ym);

            BigDecimal amount = (i < n - 1) ? per : total.subtract(acc).setScale(2, RoundingMode.HALF_UP);
            acc = acc.add(amount);

            // 1) Ajusta o total da fatura de destino
            addToBillValue(target, amount);
            changed.add(target);

            // 2) Cria o item desta parcela apontando para a fatura correta
            CreditCardBill parcela = new CreditCardBill();
            parcela.setCreditCard(creditCard);
            parcela.setBill(target);
            parcela.setCategory(category);
            parcela.setRegistrationDate(creditCardBill.getRegistrationDate()); // data da compra
            parcela.setDescription(appendParcelaSuffix(creditCardBill.getDescription(), i + 1, n));
            parcela.setValue(amount.doubleValue());
            parcela.setInstallments("sim");
            parcela.setNumberinstallments(n);
            parcela.setActive(Active.ACTIVE);

            itensParcelas.add(parcela);
        }

        if (!changed.isEmpty()) billRepository.saveAll(changed);
        List<CreditCardBill> salvos = creditCardBillRepository.saveAll(itensParcelas);

        // retorna a 1ª parcela (a da fatura corrente)
        return salvos.stream()
                .filter(cb -> cb.getBill().getUuid().equals(initialBill.getUuid()))
                .findFirst()
                .orElse(salvos.get(0));
    }

    // ================== UPDATE ==================
    /**
     * Atualiza UM item (uma parcela ou compra à vista):
     * - estorna o valor da fatura antiga
     * - troca dados/associações
     * - aplica o valor na fatura nova
     *
     * (Não redistribui grupo de parcelas; cada item é independente.)
     */
    @Transactional
    public CreditCardBill update(UUID id, CreditCardBill payload) {
        CreditCardBill persisted = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        // carregar novas associações
        CreditCard cc  = creditCardRepository.findById(payload.getCreditCard().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCreditCard().getUuid()));
        Bill bil = billRepository.findById(payload.getBill().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getBill().getUuid()));
        Category cat = categoryRepository.findById(payload.getCategory().getUuid())
                .orElseThrow(() -> new ResourceNotFoundExeption(payload.getCategory().getUuid()));

        // segurança
        User me = securityService.obterUserLogin();
        boolean owner = cc.getAccounts().getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para alterar este lançamento.");

        assertActiveForPosting(cc, bil);
        assertCategoryActive(cat);

        // 1) Estorna valor da fatura anterior
        BigDecimal antigo = BigDecimal.valueOf(persisted.getValue() == null ? 0.0 : persisted.getValue())
                .setScale(2, RoundingMode.HALF_UP);
        addToBillValue(persisted.getBill(), antigo.negate());
        billRepository.save(persisted.getBill());

        // 2) Aplica novos dados
        persisted.setCreditCard(cc);
        persisted.setBill(bil);
        persisted.setCategory(cat);
        persisted.setValue(payload.getValue());
        persisted.setDescription(payload.getDescription());
        persisted.setRegistrationDate(payload.getRegistrationDate());
        persisted.setInstallments(payload.getInstallments());
        persisted.setNumberinstallments(payload.getNumberinstallments());

        // 3) Aplica valor na nova fatura
        BigDecimal novo = BigDecimal.valueOf(persisted.getValue() == null ? 0.0 : persisted.getValue())
                .setScale(2, RoundingMode.HALF_UP);
        addToBillValue(bil, novo);
        billRepository.save(bil);

        return creditCardBillRepository.save(persisted);
    }

    // ================== DELETE ==================
    @Transactional
    public void delete(UUID id) {
        var entity = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        BigDecimal val = BigDecimal.valueOf(entity.getValue() == null ? 0.0 : entity.getValue())
                .setScale(2, RoundingMode.HALF_UP);
        addToBillValue(entity.getBill(), val.negate());
        billRepository.save(entity.getBill());

        creditCardBillRepository.delete(entity);
    }

    // ================== TOGGLES ==================
    @Transactional
    public void deactivate(UUID id) {
        var entity = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        User me = securityService.obterUserLogin();
        boolean owner = entity.getCreditCard().getAccounts().getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para desativar este lançamento.");

        if (entity.getActive() == Active.DISABLE) return; // idempotente

        BigDecimal val = BigDecimal.valueOf(entity.getValue() == null ? 0.0 : entity.getValue())
                .setScale(2, RoundingMode.HALF_UP);
        addToBillValue(entity.getBill(), val.negate());
        billRepository.save(entity.getBill());

        entity.setActive(Active.DISABLE);
        creditCardBillRepository.save(entity);
    }

    @Transactional
    public void activate(UUID id) {
        var entity = creditCardBillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExeption(id));

        User me = securityService.obterUserLogin();
        boolean owner = entity.getCreditCard().getAccounts().getUser().getUuid().equals(me.getUuid());
        if (!owner && !isAdmin(me)) throw new AccessDeniedException("Sem permissão para reativar este lançamento.");

        if (entity.getActive() == Active.ACTIVE) return; // idempotente

        assertActiveForPosting(entity.getCreditCard(), entity.getBill());
        assertCategoryActive(entity.getCategory());

        BigDecimal val = BigDecimal.valueOf(entity.getValue() == null ? 0.0 : entity.getValue())
                .setScale(2, RoundingMode.HALF_UP);
        addToBillValue(entity.getBill(), val);
        billRepository.save(entity.getBill());

        entity.setActive(Active.ACTIVE);
        creditCardBillRepository.save(entity);
    }

    // ================== HELPERS ==================
    private void addToBillValue(Bill bill, BigDecimal inc) {
        double current = bill.getValue() == null ? 0.0 : bill.getValue();
        bill.setValue(
                BigDecimal.valueOf(current).add(inc).setScale(2, RoundingMode.HALF_UP).doubleValue()
        );
    }

    private Bill findBillByMonthOrThrow(CreditCard card, YearMonth ym) {
        return billRepository.findFirstByCreditCardAndCloseDateBetween(card, ym.atDay(1), ym.atEndOfMonth())
                .orElseThrow(() -> new EntityNotFoundException("Fatura do cartão " + card.getUuid() + " não encontrada para " + ym));
    }

    private static String appendParcelaSuffix(String desc, int idx, int total) {
        String base = (desc == null || desc.isBlank()) ? "Compra" : desc.trim();
        return base + " (" + idx + "/" + total + ")";
    }
}
