package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Transactions;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.TransactionsService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/transactions")
public class TransactionsResource {

    @Autowired
    private TransactionsService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Transactions>> findAll(@RequestParam(required = false) Active active) {
        var out = (active == null) ? service.findAll() : service.findAll(active);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Transactions> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(service.findById(UUID.fromString(id)));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Transactions>> searchByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(service.searchByRegistrationDate(from, to));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> insert(@RequestBody Transactions body) {
        Transactions saved = service.insert(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getUuid())
                .toUri();
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Transação criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Transactions body) {
        service.update(UUID.fromString(id), body);
        return ResponseEntity.ok(new HttpResponseDTO("Transação atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        service.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Transação removida com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        service.deactivateSilently(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Transação desativada."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        service.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Transação ativada."));
    }
}
