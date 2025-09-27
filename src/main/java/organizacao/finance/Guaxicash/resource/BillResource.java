package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.entities.dto.PaymentRequest;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.service.BillService;
import organizacao.finance.Guaxicash.service.UserService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/bill")
public class BillResource {

    @Autowired private BillService billService;
    @Autowired private BillRepository billRepository;
    @Autowired private UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Bill>> findMyBills(
            Authentication authentication,
            @RequestParam(name="active", required = false) Active active
    ) {
        UUID userId;
        Object principal = authentication.getPrincipal();
        if (principal instanceof User u) {
            userId = u.getUuid();
        } else {
            String email = authentication.getName();
            User u = (User) userService.loadUserByUsername(email);
            userId = u.getUuid();
        }
        List<Bill> out = (active == null) ? billService.findByUserId(userId)
                : billService.findByUserId(userId, active);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<List<Bill>> findByCard(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String cardId,
            @RequestParam(name="active", required=false) Active active
    ) {
        List<Bill> out = (active == null) ? billService.findByCreditCard(UUID.fromString(cardId))
                : billService.findByCreditCard(UUID.fromString(cardId), active);
        return ResponseEntity.ok(out);
    }

    @GetMapping(params = "status")
    public ResponseEntity<List<Bill>> listByStatus(
            @RequestParam BillPay status,
            @RequestParam(required = false)
            @org.hibernate.validator.constraints.UUID(message="UUID inválido") String creditCardId,
            @RequestParam(name="active", required=false) Active active
    ) {
        Sort sort = Sort.by("payDate").ascending();
        List<Bill> list = (creditCardId == null)
                ? (active == null ? billRepository.findByStatus(status, sort)
                : billRepository.findByStatusAndActive(status, active, sort))
                : (active == null ? billRepository.findByCreditCard_UuidAndStatus(UUID.fromString(creditCardId), status, sort)
                : billRepository.findByCreditCard_UuidAndStatusAndActive(UUID.fromString(creditCardId), status, active, sort));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Bill>> listByStatuses(
            @RequestParam List<BillPay> status,
            @RequestParam(name="active", required=false) Active active
    ) {
        Sort sort = Sort.by("payDate").ascending();
        List<Bill> out = (active == null) ? billRepository.findByStatusIn(status, sort)
                : billRepository.findByStatusInAndActive(status, active, sort);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/payment/{billId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> registerPayment(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String billId,
            @RequestBody PaymentRequest body
    ) {
        billService.registerPayment(UUID.fromString(billId), body.amount());
        return ResponseEntity.ok(new HttpResponseDTO("Pagamento registrado com sucesso."));
    }

    // Soft delete local
    @DeleteMapping("/deactivate/{billId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String billId) {
        billService.deactivate(UUID.fromString(billId));
        return ResponseEntity.ok(new HttpResponseDTO("Fatura desativada."));
    }

    @PutMapping("/activate/{billId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String billId) {
        billService.activate(UUID.fromString(billId));
        return ResponseEntity.ok(new HttpResponseDTO("Fatura ativada."));
    }
}
