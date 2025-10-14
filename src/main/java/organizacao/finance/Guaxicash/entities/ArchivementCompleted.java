package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ArchivementCompleted")
public class ArchivementCompleted {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "missions_id")
    private Archivement archivement;

    public ArchivementCompleted() {

    }

    public ArchivementCompleted(UUID uuid, User user, Archivement archivement) {
        this.uuid = uuid;
        this.user = user;
        this.archivement = archivement;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Archivement getArchivement() {
        return archivement;
    }

    public void setArchivement(Archivement archivement) {
        this.archivement = archivement;
    }


}
