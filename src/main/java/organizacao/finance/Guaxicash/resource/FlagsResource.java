package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.FlagsService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/flags")
public class FlagsResource {

    @Autowired
    private FlagsService flagsService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> create(@RequestBody Flags flags) {
        Flags created = flagsService.insert(flags);
        URI location = URI.create("/flags/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Bandeira criada com sucesso."));
    }

    // Filtro por active: /flags?active=ACTIVE|DISABLE
    @GetMapping
    public ResponseEntity<List<Flags>> findAll(
            @RequestParam(name = "active", required = false) Active active
    ) {
        List<Flags> list = (active == null) ? flagsService.findAll()
                : flagsService.findAll(active);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flags> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(flagsService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Flags flags) {
        flagsService.update(UUID.fromString(id), flags);
        return ResponseEntity.ok(new HttpResponseDTO("Bandeira atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id){
        flagsService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Bandeira removida com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        flagsService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Bandeira desativada."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        flagsService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Bandeira ativada."));
    }
}
