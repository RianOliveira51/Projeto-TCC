package organizacao.finance.Guaxicash.service.eventos.CreditCardForBill;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class CreditCardCreatedEvent extends ApplicationEvent {
    private UUID creditCardId;

    public CreditCardCreatedEvent(Object source, UUID creditCardId) {
        super(source);
        this.creditCardId = creditCardId;
    }
    public UUID getCreditCardId() { return creditCardId; }

}
