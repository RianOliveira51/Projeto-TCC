package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import organizacao.finance.Guaxicash.entities.Reciphe;
import organizacao.finance.Guaxicash.service.RecipheService;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "reciphe")
public class ControlerRechipe {
    @Autowired
    private RecipheService recipheservice;

    // GET /reciphes
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reciphe>> findAll() {
        return ResponseEntity.ok(recipheservice.findAll());
    }

    // GET /reciphes/{id}
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reciphe> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipheservice.findById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reciphe>> search(
            @RequestParam(required = false) String field, // "registration" ou "pay"

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime
    ) {
        return ResponseEntity.ok(
                recipheservice.search(field, fromDate, toDate, fromDateTime, toDateTime)
        );
    }

    // POST /reciphes
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reciphe> insert(@RequestBody Reciphe body) {
        Reciphe saved = recipheservice.insert(body);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getUuid())
                .toUri();
        return ResponseEntity.created(location).body(saved); // 201 + Location
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reciphe> update(@PathVariable UUID id, @RequestBody Reciphe body) {
        return ResponseEntity.ok(recipheservice.update(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipheservice.delete(id);
        return ResponseEntity.noContent().build();
    }

}
