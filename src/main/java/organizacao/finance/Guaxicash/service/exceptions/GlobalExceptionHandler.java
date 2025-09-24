package organizacao.finance.Guaxicash.service.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponseDTO> badCredentials(HttpServletRequest req, BadCredentialsException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos", req, ex, false);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HttpResponseDTO> authentication(HttpServletRequest req, AuthenticationException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Usuário não autenticado", req, ex, false);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponseDTO> accessDenied(HttpServletRequest req, AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "Acesso negado", req, ex, false);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponseDTO> tokenExpired(HttpServletRequest req, TokenExpiredException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Sessão expirada. Faça login novamente.", req, ex, false);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<HttpResponseDTO> jwtInvalid(HttpServletRequest req, JWTVerificationException ex) {
        return body(HttpStatus.UNAUTHORIZED, "Token inválido", req, ex, false);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponseDTO> beanValidation(HttpServletRequest req, MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "inválido" : fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        if (msg.isBlank()) msg = "Requisição inválida";
        return body(HttpStatus.BAD_REQUEST, msg, req, ex, false);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<HttpResponseDTO> constraintViolation(
            HttpServletRequest req,
            jakarta.validation.ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map((jakarta.validation.ConstraintViolation<?> v) ->
                        (v.getPropertyPath() == null ? "" : v.getPropertyPath().toString()) + ": " + v.getMessage())
                .collect(java.util.stream.Collectors.joining("; "));
        if (msg.isBlank()) msg = "Requisição inválida";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HttpResponseDTO(msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HttpResponseDTO> unreadable(HttpServletRequest req, HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "JSON malformado ou tipo inválido no corpo da requisição", req, ex, false);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<HttpResponseDTO> missingParam(HttpServletRequest req, MissingServletRequestParameterException ex) {
        return body(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName(), req, ex, false);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpResponseDTO> paramTypeMismatch(HttpServletRequest req, MethodArgumentTypeMismatchException ex) {
        return body(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName(), req, ex, false);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<HttpResponseDTO> notFound(HttpServletRequest req, EntityNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Recurso não encontrado", req, ex, false);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<HttpResponseDTO> integrity(HttpServletRequest req, DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT, "Violação de integridade de dados", req, ex, true);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponseDTO> methodNotAllowed(HttpServletRequest req, HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido", req, ex, false);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<HttpResponseDTO> noHandler(HttpServletRequest req, NoHandlerFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "Endpoint não encontrado", req, ex, false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponseDTO> generic(HttpServletRequest req, Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", req, ex, true);
    }

    private ResponseEntity<HttpResponseDTO> body(HttpStatus status, String message, HttpServletRequest req, Exception ex, boolean logStack) {
        if (logStack) {
            log.error("[{}] {} -> {}", status.value(), req.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("[{}] {} -> {}", status.value(), req.getRequestURI(), ex.getMessage());
        }
        return ResponseEntity.status(status).body(new HttpResponseDTO(message));
    }
}
