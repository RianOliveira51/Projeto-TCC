package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Accounts;
import organizacao.finance.Guaxicash.entities.Bill;
import organizacao.finance.Guaxicash.entities.Flags;
import organizacao.finance.Guaxicash.repositories.BillRepository;
import organizacao.finance.Guaxicash.repositories.FlagsRepository;
import organizacao.finance.Guaxicash.service.BillService;
import organizacao.finance.Guaxicash.service.FlagsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/flags")
public class FlagsResource {

    @Autowired
    private FlagsService flagsService;
    @Autowired
    private FlagsRepository flagsRepository;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Flags> create(@RequestBody Flags flags) {
        flags = flagsService.insert(flags);
        return ResponseEntity.ok(flags);
    }

    @GetMapping
    public ResponseEntity<List<Flags>> findAll() {
        List<Flags> list = flagsService.findAll();
        return ResponseEntity.ok().body(list);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Flags> updateAccount(@PathVariable UUID id, @RequestBody Flags flags) {
        Flags updated = flagsService.update(id, flags);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity delete(@PathVariable UUID id){
        flagsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
