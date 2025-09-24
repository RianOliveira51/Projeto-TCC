package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import organizacao.finance.Guaxicash.entities.CreditCard;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.CreditCardService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/creditCard")
public class CreditCardResource {

    @Autowired
    private CreditCardService creditCardService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()") // ajuste para hasRole('ADMIN') se preferir
    public ResponseEntity<HttpResponseDTO> create(@RequestBody CreditCard creditCard) {
        CreditCard created = creditCardService.insert(creditCard);
        URI location = URI.create("/creditCard/" + created.getUuid());
        return ResponseEntity.created(location)
                .body(new HttpResponseDTO("Cartão criado com sucesso."));
    }

    @GetMapping
    public ResponseEntity<List<CreditCard>> findAll() {
        return ResponseEntity.ok(creditCardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCard> findById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id) {
        return ResponseEntity.ok(creditCardService.findById(UUID.fromString(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id,
            @RequestBody CreditCard creditCard){
        creditCardService.updateCard(UUID.fromString(id), creditCard);
        return ResponseEntity.ok(new HttpResponseDTO("Cartão atualizado com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message = "UUID inválido") String id){
        creditCardService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Cartão removido com sucesso."));
    }
}
