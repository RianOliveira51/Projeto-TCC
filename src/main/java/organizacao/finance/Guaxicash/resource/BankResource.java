package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.service.BankService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "bank")
public class BankResource {

    @Autowired
    private BankService bankService;
    @Autowired
    private BankRepository bankRepository;


    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Bank> createAccount(@RequestBody Bank bank) {
        bank = bankService.insert(bank);
        return ResponseEntity.ok(bank);
    }


    @PutMapping({"/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Bank> updateBank(@PathVariable UUID id, @RequestBody Bank bank){
        Bank updated = bankService.update(id, bank);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Bank>> findAll() {
        List<Bank> list = bankService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        bankService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
