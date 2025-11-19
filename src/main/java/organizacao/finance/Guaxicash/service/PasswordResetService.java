package organizacao.finance.Guaxicash.service;

import organizacao.finance.Guaxicash.entities.PasswordResetToken;
import organizacao.finance.Guaxicash.entities.User;
import organizacao.finance.Guaxicash.entities.dto.ForgotPasswordRequest;
import organizacao.finance.Guaxicash.entities.dto.ResetPasswordRequest;
import organizacao.finance.Guaxicash.repositories.PasswordResetTokenRepository;
import organizacao.finance.Guaxicash.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ========= PASSO 1: esqueceu a senha =========
    public void requestReset(ForgotPasswordRequest request) {
        // aqui usamos o mesmo padrão que você já usa no SecurityService/UserService:
        UserDetails ud = userRepository.findByEmail(request.email());
        if (ud == null) {
            // Por segurança, não revelamos se o e-mail existe ou não
            return;
        }

        // sua entidade User implementa UserDetails, então o cast é seguro
        User user = (User) ud;

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken prt = new PasswordResetToken(token, user, expiresAt);
        tokenRepository.save(prt);

        String resetLink = frontendUrl + "/reset-password/" + token;

        String subject = "Recuperação de senha - Guaxicash";
        String body = """
                Olá, %s!

                Recebemos uma solicitação para redefinir sua senha no Guaxicash.

                Clique no link abaixo para criar uma nova senha:
                %s

                Este link é válido por 30 minutos.

                Se você não solicitou, apenas ignore este e-mail.
                """.formatted(user.getName(), resetLink);

        emailService.send(user.getEmail(), subject, body);
    }

    // ========= PASSO 2: resetar a senha =========
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (token.isUsed()) {
            throw new RuntimeException("Token já utilizado");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
