package organizacao.finance.Guaxicash.entities.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
