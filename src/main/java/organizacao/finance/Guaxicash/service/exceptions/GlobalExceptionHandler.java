package organizacao.finance.Guaxicash.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;


import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<HttpResponseDTO> body(HttpStatus status, String msg) {
        return ResponseEntity.status(status).body(new HttpResponseDTO(msg));
    }

    // 400 – validação / request inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return body(HttpStatus.BAD_REQUEST, msg.isBlank() ? "Requisição inválida" : msg);
    }

    // 401 – não autenticado / credenciais inválidas / usuário desativado
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<HttpResponseDTO> handleAuth(Exception ex) {
        String msg = (ex instanceof DisabledException) ? "Usuário desativado" : "Usuário ou senha inválidos";
        return body(HttpStatus.UNAUTHORIZED, msg);
    }

    // 403 – sem permissão
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<HttpResponseDTO> handleSecurity(SecurityException ex) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 404 – não encontrado
    @ExceptionHandler(ResourceNotFoundExeption.class)
    public ResponseEntity<HttpResponseDTO> handleNotFound(ResourceNotFoundExeption ex) {
        return body(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    // 409 – conflito de estado (regras de negócio)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<HttpResponseDTO> handleIllegalState(IllegalStateException ex) {
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }


    // 500 – fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponseDTO> handleGeneric(Exception ex) {
        // logue ex aqui (logger.error("Erro inesperado", ex))
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }
}
