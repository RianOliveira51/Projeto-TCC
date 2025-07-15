package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Flags")
public class Flags {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String name;

    public Flags() {

    }

    public Flags(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
