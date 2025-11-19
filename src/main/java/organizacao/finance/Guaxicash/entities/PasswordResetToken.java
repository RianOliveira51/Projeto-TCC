package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, User user, LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    // getters e setters

    public Long getId() { return id; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }

    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }

    public void setUsed(boolean used) { this.used = used; }
}
