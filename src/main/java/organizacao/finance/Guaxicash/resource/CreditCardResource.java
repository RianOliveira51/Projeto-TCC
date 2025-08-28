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

    @PostMapping("/create")
    public ResponseEntity<CreditCard> create(@RequestBody CreditCard creditCard) {
        CreditCard created = creditCardService.insert(creditCard);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<CreditCard>> findAll() {
        return ResponseEntity.ok(creditCardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCard> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(creditCardService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCard> update(@PathVariable UUID id, @RequestBody CreditCard creditCard){
        return ResponseEntity.ok(creditCardService.update(id, creditCard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        creditCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
