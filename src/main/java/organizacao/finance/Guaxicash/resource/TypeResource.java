package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.BankService;
import organizacao.finance.Guaxicash.service.TypeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/type")
public class TypeResource {

    @Autowired
    private TypeService typeService;
    @Autowired
    private TypeRepository typeRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Type> createAccount(@RequestBody Type type) {
        type = typeService.insert(type);
        return ResponseEntity.ok(type);
    }

    @GetMapping
    public ResponseEntity<List<Type>> findAll() {
        List<Type> list = typeService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @PutMapping({"/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Type> updateBank(@PathVariable UUID id, @RequestBody Type type){
        Type updated = typeService.update(id, type);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity delete(@PathVariable UUID id){
        typeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
