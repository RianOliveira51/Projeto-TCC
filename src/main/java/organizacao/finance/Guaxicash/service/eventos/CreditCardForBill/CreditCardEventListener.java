package organizacao.finance.Guaxicash.service.eventos.CreditCardForBill;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import organizacao.finance.Guaxicash.repositories.CreditCardRepository;
import organizacao.finance.Guaxicash.service.BillService;

@Component
public class CreditCardEventListener implements ApplicationListener<CreditCardCreatedEvent> {
    private CreditCardRepository creditCardRepository;
    private BillService billService;

    public CreditCardEventListener(CreditCardRepository ccRepo, BillService billService) {
        this.creditCardRepository = ccRepo; this.billService = billService;
    }

    @Override
    public void onApplicationEvent(CreditCardCreatedEvent event) {
        creditCardRepository.findById(event.getCreditCardId())
                .ifPresent(billService::generateBillsUntilDec2025); // retorna void
    }
}
