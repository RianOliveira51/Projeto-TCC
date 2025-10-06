package organizacao.finance.Guaxicash.config;

import jakarta.servlet.http.HttpServletRequest;
import organizacao.finance.Guaxicash.entities.dto.HttpResponseDTO;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = "/error", produces = "application/json")
    public ResponseEntity<HttpResponseDTO> handleError(HttpServletRequest request) {
        // Converter HttpServletRequest -> WebRequest
        WebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> attrs =
                errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        int status = (int) attrs.getOrDefault("status", 500);

        String message = switch (status) {
            case 404 -> "Essa rota não existe.";
            case 405 -> "Esse método HTTP não é permitido para esta rota.";
            default   -> "Erro interno do servidor";
        };

        return ResponseEntity.status(status).body(new HttpResponseDTO(message));
    }
}
