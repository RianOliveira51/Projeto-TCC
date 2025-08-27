package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.repositories.CreditCardRepository;
import organizacao.finance.Guaxicash.service.BillService;
import organizacao.finance.Guaxicash.service.CreditCardService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creditCard")
public class CreditCardResource {

    @Autowired
    private CreditCardService creditCardService;
    @Autowired
    private CreditCardRepository creditCardRepository;

    @PostMapping("/create")
    public ResponseEntity<CreditCard> create(@RequestBody CreditCard creditCard) {
        creditCard = creditCardService.insert(creditCard);
        return ResponseEntity.ok(creditCard);
    }

    @GetMapping
    public ResponseEntity<List<CreditCard>> findAll() {
        List<CreditCard> list = creditCardService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @PutMapping({"/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CreditCard> updateBank(@PathVariable UUID id, @RequestBody CreditCard creditCard){
        CreditCard updated = creditCardService.update(id, creditCard);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        creditCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
