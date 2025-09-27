package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.TypeService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/type")
public class TypeResource {

    @Autowired
    private TypeService typeService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> createType(@RequestBody Type type) {
        Type created = typeService.insert(type);
        URI location = URI.create("/type/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Tipo criado com sucesso."));
    }

    // Suporta filtro por active: /type?active=ACTIVE|DISABLE
    @GetMapping
    public ResponseEntity<List<Type>> findAll(
            @RequestParam(name = "active", required = false) Active active
    ) {
        List<Type> list = (active == null) ? typeService.findAll()
                : typeService.findAll(active);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Type> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(typeService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> updateType(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Type type){
        typeService.update(UUID.fromString(id), type);
        return ResponseEntity.ok(new HttpResponseDTO("Tipo atualizado com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id){
        typeService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Tipo removido com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id){
        typeService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Tipo desativado."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id){
        typeService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Tipo ativado."));
    }
}
