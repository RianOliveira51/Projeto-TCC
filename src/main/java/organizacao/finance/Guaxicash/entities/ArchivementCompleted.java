package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ArchivementCompleted",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_arch_completed_user_arch", columnNames = {"user_id", "archivement_id"})
        }
)
public class ArchivementCompleted {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "archivement_id", nullable = false)
    private Archivement archivement;

    @Column(nullable = false)
    private LocalDate completedAt = LocalDate.now();

    public ArchivementCompleted() {}

    public ArchivementCompleted(UUID uuid, User user, Archivement archivement) {
        this.uuid = uuid;
        this.user = user;
        this.archivement = archivement;
        this.completedAt = LocalDate.now();
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Archivement getArchivement() { return archivement; }
    public void setArchivement(Archivement archivement) { this.archivement = archivement; }
    public LocalDate getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDate completedAt) { this.completedAt = completedAt; }
}
