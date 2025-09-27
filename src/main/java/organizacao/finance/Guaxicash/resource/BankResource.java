package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.Bank;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.BankService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/bank")
public class BankResource {

    @Autowired
    private BankService bankService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> createBank(@RequestBody Bank bank) {
        Bank created = bankService.insert(bank);
        URI location = URI.create("/bank/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Banco criado com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> updateBank(
            @PathVariable UUID id, @RequestBody Bank bank) {
        bankService.update(id, bank);
        return ResponseEntity.ok(new HttpResponseDTO("Banco atualizado com sucesso."));
    }

    // Filtro opcional por active
    @GetMapping
    public ResponseEntity<List<Bank>> findAll(
            @RequestParam(name = "active", required = false) Active active
    ) {
        List<Bank> list = (active == null) ? bankService.findAll()
                : bankService.findAll(active);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bank> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(bankService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> delete(@PathVariable UUID id) {
        bankService.delete(id);
        return ResponseEntity.ok(new HttpResponseDTO("Banco removido com sucesso."));
    }

    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> deactivate(@PathVariable UUID id) {
        bankService.deactivate(id);
        return ResponseEntity.ok(new HttpResponseDTO("Banco desativado."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<HttpResponseDTO> activate(@PathVariable UUID id) {
        bankService.activate(id);
        return ResponseEntity.ok(new HttpResponseDTO("Banco ativado."));
    }
}
