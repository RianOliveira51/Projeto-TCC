package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Category;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.CategoryService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/category")
public class CategoryResource {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> create(@RequestBody Category category) {
        Category created = categoryService.insert(category);
        URI location = URI.create("/category/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Categoria criada com sucesso."));
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(
            @RequestParam(required = false) String earn,
            @RequestParam(required = false) Active active
    ) {
        List<Category> list;
        if (earn == null && active == null) {
            list = categoryService.findAll();
        } else if (earn == null) {
            list = categoryService.findAll(active);
        } else {
            Boolean parsed = parseEarnOrThrow(earn);
            list = (active == null)
                    ? categoryService.findByEarn(parsed)
                    : categoryService.findByEarn(parsed, active);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(categoryService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody Category category) {
        categoryService.update(UUID.fromString(id), category);
        return ResponseEntity.ok(new HttpResponseDTO("Categoria atualizada com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        categoryService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Categoria removida com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        categoryService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Categoria desativada."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        categoryService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Categoria ativada."));
    }

    private static Boolean parseEarnOrThrow(String v) {
        String s = v.trim().toLowerCase();
        return switch (s) {
            case "1", "true", "t", "yes", "y" -> Boolean.TRUE;
            case "0", "false", "f", "no", "n" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Parâmetro 'earn' inválido. Use 0/1 ou true/false.");
        };
    }
}
