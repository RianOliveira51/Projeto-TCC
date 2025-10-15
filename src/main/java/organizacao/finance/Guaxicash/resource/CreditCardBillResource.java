package organizacao.finance.Guaxicash.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import organizacao.finance.Guaxicash.entities.CreditCardBill;
import organizacao.finance.Guaxicash.entities.Enums.Active;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import organizacao.finance.Guaxicash.service.CreditCardBillService;
import organizacao.finance.Guaxicash.service.exceptions.ResourceNotFoundExeption;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/creditCardBill")
public class CreditCardBillResource {

    @Autowired private CreditCardBillService creditCardBillService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CreditCardBill>> findAll(
            @RequestParam(name="active", required=false) Active active
    ) {
        List<CreditCardBill> list = (active == null)
                ? creditCardBillService.findAll()
                : creditCardBillService.findAll(active);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreditCardBill> getById(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        return ResponseEntity.ok(creditCardBillService.findById(UUID.fromString(id)));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CreditCardBill>> search(
            @RequestParam(required=false) String field,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required=false) @org.hibernate.validator.constraints.UUID(message="UUID inválido") String creditCardId
    ) {
        UUID ccId = (creditCardId == null ? null : UUID.fromString(creditCardId));
        return ResponseEntity.ok(creditCardBillService.searchByDate(field, from, to, ccId));
    }

    // Por fatura
    @GetMapping("/bill/{billId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CreditCardBill>> findByBill(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String billId,
            @RequestParam(name="active", required=false) Active active
    ) {
        List<CreditCardBill> list = creditCardBillService.findAllByBill(UUID.fromString(billId), active);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> create(@RequestBody CreditCardBill creditCardBill) {
        CreditCardBill created = creditCardBillService.insert(creditCardBill);
        URI location = URI.create("/creditCardBill/" + created.getUuid());
        return ResponseEntity.created(location).body(new HttpResponseDTO("Lançamento criado com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> update(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id,
            @RequestBody CreditCardBill payload
    ) {
        creditCardBillService.update(UUID.fromString(id), payload);
        return ResponseEntity.ok(new HttpResponseDTO("Lançamento atualizado com sucesso."));
    }

    // Hard delete
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> delete(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardBillService.delete(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Lançamento removido com sucesso."));
    }

    // Toggle (dono ou admin) com ajuste nas faturas
    @DeleteMapping("/deactivate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> deactivate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardBillService.deactivate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Lançamento desativado e valores estornados nas faturas."));
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HttpResponseDTO> activate(
            @PathVariable @org.hibernate.validator.constraints.UUID(message="UUID inválido") String id) {
        creditCardBillService.activate(UUID.fromString(id));
        return ResponseEntity.ok(new HttpResponseDTO("Lançamento reativado e valores reaplicados nas faturas."));
    }

    /* ===================== Exception Handlers ===================== */

    // Regra de negócio / validação de estado (ex.: CLOSE_PENDING / janela encerrada)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<HttpResponseDTO> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new HttpResponseDTO(ex.getMessage())); // 422
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponseDTO> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler({ResourceNotFoundExeption.class, NoSuchElementException.class})
    public ResponseEntity<HttpResponseDTO> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new HttpResponseDTO(ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponseDTO> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new HttpResponseDTO(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<HttpResponseDTO> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new HttpResponseDTO("Conflito de integridade: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpResponseDTO("Parâmetro inválido: " + ex.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponseDTO> handleGeneric(Exception ex) {
        // Logue ex para diagnóstico; aqui retornamos 500 genérico
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HttpResponseDTO("Erro interno ao processar a requisição."));
    }
}
