package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.BillService;
import organizacao.finance.Guaxicash.service.TypeService;
import organizacao.finance.Guaxicash.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bill")
public class BillResource {

    @Autowired
    private BillService billService;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Bill> create(@RequestBody Bill bill) {
        bill = billService.insert(bill);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Bill>> findMyBills(Authentication authentication) {
        UUID userId;

        Object principal = authentication.getPrincipal();
        if (principal instanceof User u) {
            userId = u.getUuid();
        } else {
            String email = authentication.getName();
            User u = (User) userService.loadUserByUsername(email); // seu UserService implementa UserDetailsService
            userId = u.getUuid();
        }

        return ResponseEntity.ok(billService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Bill>> findByCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(billService.findByCreditCard(cardId));
    }
}
