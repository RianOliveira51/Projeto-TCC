package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.entities.dto.TotalBalanceResponse;
import organizacao.finance.Guaxicash.service.AccountsService;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/accounts")
public class AccountsResource {

    @Autowired
    private AccountsService accountsService;

    @PostMapping("/create")
    public ResponseEntity<HttpResponseDTO> createAccount(@RequestBody Accounts accounts) {
        Accounts created = accountsService.insert(accounts);
        URI location = URI.create("/accounts/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Conta criada com sucesso."));
    }

    @GetMapping("/total-balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TotalBalanceResponse> getMyTotalBalance() {
        Double total = accountsService.totalBalanceOfLogged();
        return ResponseEntity.ok(new TotalBalanceResponse(total));
    }

    @GetMapping
    public ResponseEntity<List<Accounts>> findAll() {
        return ResponseEntity.ok(accountsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accounts> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(accountsService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpResponseDTO> updateAccount(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Accounts accounts) {
        accountsService.update(UUID.fromString(id), accounts);
        return ResponseEntity.ok(new HttpResponseDTO("Conta atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        accountsService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Conta removida com sucesso."));
    }
}
