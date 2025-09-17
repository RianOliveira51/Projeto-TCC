package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import organizacao.finance.Guaxicash.entities.Expenses;
import organizacao.finance.Guaxicash.service.ExpenseService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "expense")
public class ExpenseResource {
    @Autowired
    private ExpenseService expenseServiceservice;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expenses>> findAll() {
        return ResponseEntity.ok(expenseServiceservice.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expenses> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseServiceservice.findById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Expenses>> searchByPayDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(expenseServiceservice.searchByPayDate(from, to));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expenses> insert(@RequestBody Expenses body) {
        Expenses saved = expenseServiceservice.insert(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Expenses> update(@PathVariable UUID id, @RequestBody Expenses body) {
        return ResponseEntity.ok(expenseServiceservice.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        expenseServiceservice.delete(id);
        return ResponseEntity.noContent().build();
    }
}
