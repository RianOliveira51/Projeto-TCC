package organizacao.finance.Guaxicash.service.exceptions;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;

public class ApiErrorController implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<HttpResponseDTO> handleError(HttpServletRequest req) {
        Object statusAttr = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = (statusAttr instanceof Integer) ? (Integer) statusAttr : 500;

        if (status == HttpStatus.NOT_FOUND.value()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new HttpResponseDTO("Endpoint não encontrado"));
        }
        if (status == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(new HttpResponseDTO("Método HTTP não permitido"));
        }

        // fallback
        return ResponseEntity.status(status)
                .body(new HttpResponseDTO("Erro"));
    }
}
