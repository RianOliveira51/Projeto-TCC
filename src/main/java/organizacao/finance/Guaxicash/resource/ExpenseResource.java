package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Expenses;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.ExpenseService;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/expense")
public class ExpenseResource {

    @Autowired
    private ExpenseService expenseServiceservice;

    // Listagem com filtro opcional de status
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expenses>> findAll(@RequestParam(required = false) Active active) {
        List<Expenses> out = (active == null) ? expenseServiceservice.findAll()
                : expenseServiceservice.findAll(active);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expenses> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(expenseServiceservice.findById(UUID.fromString(id)));
    }

    @GetMapping("/total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> totalAmount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Active active
    ) {
        return ResponseEntity.ok(expenseServiceservice.totalForLogged(from, to, active));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expenses>> searchByPayDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(expenseServiceservice.searchBydateRegistration(from, to));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> insert(@RequestBody Expenses body) {
        Expenses saved = expenseServiceservice.insert(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getUuid())
                .toUri();
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Despesa criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Expenses body) {
        expenseServiceservice.update(UUID.fromString(id), body);
        return ResponseEntity.ok(new HttpResponseDTO("Despesa atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        expenseServiceservice.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Despesa removida com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        expenseServiceservice.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Despesa desativada."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        expenseServiceservice.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Despesa ativada."));
    }
}
