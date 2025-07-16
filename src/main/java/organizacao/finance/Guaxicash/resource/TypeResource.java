package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Type;
import organizacao.finance.Guaxicash.repositories.BankRepository;
import organizacao.finance.Guaxicash.repositories.TypeRepository;
import organizacao.finance.Guaxicash.service.BankService;
import organizacao.finance.Guaxicash.service.TypeService;

import java.util.List;

@RestController
@RequestMapping(value = "/type")
public class TypeResource {

    @Autowired
    private TypeService typeService;
    @Autowired
    private TypeRepository typeRepository;

    @PostMapping("/create")
    public ResponseEntity<Type> createAccount(@RequestBody Type type) {
        type = typeService.insert(type);
        return ResponseEntity.ok(type);
    }

    @GetMapping
    public ResponseEntity<List<Type>> findAll() {
        List<Type> list = typeService.findAll();
        return ResponseEntity.ok().body(list);
    }

}
