package organizacao.finance.Guaxicash.resource;

import organizacao.finance.Guaxicash.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailTestController {

    private final EmailService emailService;

    public MailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/mail-test")
    public String sendTest(@RequestParam String to) {
        emailService.send(
                to,
                "Teste SMTP Guaxicash",
                "Se você recebeu este e-mail, o SMTP está funcionando 😄"
        );
        return "OK, tentei enviar para " + to;
    }
}
