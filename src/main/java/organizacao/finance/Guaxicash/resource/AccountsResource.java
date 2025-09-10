package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.repositories.AccountsRepository;
import organizacao.finance.Guaxicash.service.AccountsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/accounts")
public class AccountsResource {

    @Autowired
    private AccountsService accountsService;

    @PostMapping("/create")
    public ResponseEntity<Accounts> createAccount(@RequestBody Accounts accounts) {
        Accounts created = accountsService.insert(accounts);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Accounts>> findAll() {
        return ResponseEntity.ok(accountsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accounts> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountsService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Accounts> updateAccount(@PathVariable UUID id, @RequestBody Accounts accounts) {
        return ResponseEntity.ok(accountsService.update(id, accounts));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        accountsService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
