package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private AccountsRepository accountsRepository;

    @PostMapping("/create")
    public ResponseEntity<Accounts> createAccount(@RequestBody Accounts accounts) {
        accounts = accountsService.insert(accounts);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Accounts> updateAccount(@PathVariable UUID id, @RequestBody Accounts accounts) {
        Accounts updated = accountsService.update(id, accounts);
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public ResponseEntity<List<Accounts>> findAll() {
        List<Accounts> list = accountsService.findAll();
        return ResponseEntity.ok().body(list);
    }
}
