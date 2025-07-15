package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Type")
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String Description;

    public Type(){

    }

    public Type(UUID uuid, String Description){
        this.uuid = uuid;
        this.Description = Description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

}
