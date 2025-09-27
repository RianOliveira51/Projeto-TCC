package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;

import java.util.UUID;

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String description;
    private boolean earn;
    private Active active = Active.ACTIVE;

    public Category() {

    }

    public Category(UUID uuid, String description, boolean earn, Active active) {
        this.uuid = uuid;
        this.description = description;
        this.earn = earn;
        this.active = active;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEarn() {
        return earn;
    }

    public void setEarn(boolean earn) {
        this.earn = earn;
    }

    public Active isActive() {
        return active;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }
}
