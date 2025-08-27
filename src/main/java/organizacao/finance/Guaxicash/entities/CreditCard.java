package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "CreditCard")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Double limite;
    private String description;
    private LocalDate closeDate;
    private LocalDate expiryDate;
    @ManyToOne
    @JoinColumn(name = "flags_id")
    private Flags flags;

    @ManyToOne
    @JoinColumn(name = "accounts_id")
    private Accounts accounts;

    public CreditCard() {

    }

    public CreditCard(UUID uuid, Double limite, String description, LocalDate closeDate, LocalDate expiryDate, Flags flags, Accounts accounts) {
        this.uuid = uuid;
        this.limite = limite;
        this.description = description;
        this.closeDate = closeDate;
        this.expiryDate = expiryDate;
        this.flags = flags;
        this.accounts = accounts;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Double getLimite() {
        return limite;
    }

    public void setLimite(Double limite) {
        this.limite = limite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }
}
