package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.service.CreditCardBillService;

import java.time.LocalDate;
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

    @GetMapping(value = "/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CreditCardBill>> search(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID creditCardId
    ) {
        List<CreditCardBill> out = creditCardBillService.searchByDate(field, from, to, creditCardId);
        return ResponseEntity.ok(out);
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
