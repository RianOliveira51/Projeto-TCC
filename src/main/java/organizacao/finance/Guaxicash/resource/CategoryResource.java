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

    @PutMapping({"/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Category> updateBank(@PathVariable UUID id, @RequestBody Category category){
        Category updated = categoryService.update(id, category);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll() {
        List<Category> list = categoryService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
