package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.CreditCardService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/creditCard")
public class CreditCardResource {

    @Autowired private CreditCardService creditCardService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> create(@RequestBody CreditCard creditCard) {
        CreditCard created = creditCardService.insert(creditCard);
        URI location = URI.create("/creditCard/" + created.getUuid());
        return ResponseEntity.created(location).body(new HttpResponseDTO("Cartão criado com sucesso."));
    }

    @GetMapping
    public ResponseEntity<List<CreditCard>> findAll(@RequestParam(name="active", required=false) Active active) {
        List<CreditCard> list = (active == null) ? creditCardService.findAll()
                : creditCardService.findAll(active);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCard> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        return ResponseEntity.ok(creditCardService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id,
            @RequestBody CreditCard creditCard) {
        creditCardService.updateCard(UUID.fromString(id), creditCard);
        return ResponseEntity.ok(new HttpResponseDTO("Cartão atualizado com sucesso."));
    }

    // Hard delete – pode trocar para ADMIN se desejar
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Cartão removido com sucesso."));
    }

    // Toggle + cascata
    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Cartão desativado. Faturas associadas também foram desativadas."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Cartão ativado. Faturas associadas também foram ativadas."));
    }
}
