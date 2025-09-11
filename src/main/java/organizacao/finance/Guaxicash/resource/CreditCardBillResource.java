package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.service.CreditCardBillService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value ="/creditCardBill")
public class CreditCardBillResource {

    @Autowired
    private CreditCardBillService creditCardBillService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CreditCardBill>> findAll() {
        return ResponseEntity.ok(creditCardBillService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CreditCardBill> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(creditCardBillService.findById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<CreditCardBill> createBank(@RequestBody CreditCardBill creditCardBill) {
        creditCardBill = creditCardBillService.insert(creditCardBill);
        return ResponseEntity.ok(creditCardBill);
    }


    @PutMapping("/{id}")
    public ResponseEntity<CreditCardBill> update(
            @PathVariable UUID id,
            @RequestBody CreditCardBill payload
    ) {
        return ResponseEntity.ok(creditCardBillService.update(id, payload));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        creditCardBillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
