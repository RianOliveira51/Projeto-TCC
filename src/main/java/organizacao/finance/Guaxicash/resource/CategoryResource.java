package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Category;
import organizacao.finance.Guaxicash.repositories.CategoryRepository;
import organizacao.finance.Guaxicash.service.CategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/category")
public class CategoryResource {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Category> createBank(@RequestBody Category category) {
        category = categoryService.insert(category);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(
            @RequestParam(required = false) String earn // aceita "0/1/true/false"
    ) {
        List<Category> list;
        if (earn == null) {
            list = categoryService.findAll();
        } else {
            Boolean parsed = parseEarnNullable(earn);
            list = categoryService.findByEarn(parsed);
        }
        return ResponseEntity.ok(list);
    }

    @PutMapping({"/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Category> updateBank(@PathVariable UUID id, @RequestBody Category category){
        Category updated = categoryService.update(id, category);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static Boolean parseEarnNullable(String v) {
        String s = v.trim().toLowerCase();
        return switch (s) {
            case "1", "true", "t", "yes", "y" -> Boolean.TRUE;
            case "0", "false", "f", "no", "n" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Parâmetro 'earn' inválido. Use 0/1 ou true/false.");
        };
    }
}
