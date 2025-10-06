package organizacao.finance.Guaxicash.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.NoHandlerFoundException;
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

    // 404 – rota/endpoint inexistente
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponseDTO> handleNoHandler(NoHandlerFoundException ex) {
        // ex.getHttpMethod(), ex.getRequestURL() disponíveis se quiser logar
        return body(HttpStatus.NOT_FOUND, "Essa rota não existe.");
    }

    // 405 – método HTTP não suportado para a rota
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        // Pode incluir ex.getMethod() e ex.getSupportedHttpMethods() no log
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Esse método HTTP não é permitido para esta rota.");
    }

    // (opcional) 403 específico do Spring Security
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<HttpResponseDTO> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "Sem permissão para acessar este recurso.");
    }


    // 500 – fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponseDTO> handleGeneric(Exception ex) {
        // logue ex aqui (logger.error("Erro inesperado", ex))
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }
}
