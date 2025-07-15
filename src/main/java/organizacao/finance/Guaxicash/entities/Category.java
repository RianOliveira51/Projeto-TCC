package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String description;

    public Category() {

    }

    public Category(UUID uuid, String description) {
        this.uuid = uuid;
        this.description = description;
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
}
