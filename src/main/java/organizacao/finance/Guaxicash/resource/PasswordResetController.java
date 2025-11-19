package organizacao.finance.Guaxicash.resource;

import organizacao.finance.Guaxicash.entities.dto.ForgotPasswordRequest;
import organizacao.finance.Guaxicash.entities.dto.ResetPasswordRequest;
import organizacao.finance.Guaxicash.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        // sempre retorna 200, mesmo se o email não existir
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
