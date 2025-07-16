package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.service.BankService;

import java.util.List;

@RestController
@RequestMapping(value = "bank")
public class BankResource {

    @Autowired
    private BankService bankService;
    @Autowired
    private BankRepository bankRepository;

    @PostMapping("/create")
    public ResponseEntity<Bank> createAccount(@RequestBody Bank bank) {
        bank = bankService.insert(bank);
        return ResponseEntity.ok(bank);
    }

    @GetMapping
    public ResponseEntity<List<Bank>> findAll() {
        List<Bank> list = bankService.findAll();
        return ResponseEntity.ok().body(list);
    }

}
