package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.BillService;
import organizacao.finance.Guaxicash.service.TypeService;

import java.util.List;

@RestController
@RequestMapping("/bill")
public class BillResource {

    @Autowired
    private BillService billService;
    @Autowired
    private BillRepository billRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Bill> create(@RequestBody Bill bill) {
        bill = billService.insert(bill);
        return ResponseEntity.ok(bill);
    }

    @GetMapping
    public ResponseEntity<List<Bill>> findAll() {
        List<Bill> list = billService.findAll();
        return ResponseEntity.ok().body(list);
    }
}
