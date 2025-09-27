package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Reciphe;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.RecipheService;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/reciphe")
public class RecipheResource {
    @Autowired
    private RecipheService recipheService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reciphe>> findAll(@RequestParam(required = false) Active active) {
        var out = (active == null) ? recipheService.findAll() : recipheService.findAll(active);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Reciphe> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(recipheService.findById(UUID.fromString(id)));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reciphe>> search(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime
    ) {
        return ResponseEntity.ok(
                recipheService.search(field, fromDate, toDate, fromDateTime, toDateTime)
        );
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> insert(@RequestBody Reciphe body) {
        Reciphe saved = recipheService.insert(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getUuid())
                .toUri();
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Receita criada com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Reciphe body) {
        recipheService.update(UUID.fromString(id), body);
        return ResponseEntity.ok(new HttpResponseDTO("Receita atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        recipheService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Receita removida com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        recipheService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Receita desativada."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        recipheService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Receita ativada."));
    }
}
