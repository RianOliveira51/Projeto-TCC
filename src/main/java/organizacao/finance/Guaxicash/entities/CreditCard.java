package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "CreditCard")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String limite;
    private String description;
    private Date CloseDate;
    private Date ExpiryDate;
    @ManyToOne
    @JoinColumn(name = "flags_id")
    private Flags flags;
    @ManyToOne
    @JoinColumn(name = "accounts_id")
    private Accounts accounts;

    public CreditCard() {

    }

    public CreditCard(UUID uuid, String limite, String description, Date closeDate, Date expiryDate, Flags flags, Accounts accounts) {
        this.uuid = uuid;
        this.limite = limite;
        this.description = description;
        this.CloseDate = closeDate;
        this.ExpiryDate = expiryDate;
        this.flags = flags;
        this.accounts = accounts;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLimite() {
        return limite;
    }

    public void setLimite(String limit) {
        this.limite = limit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCloseDate() {
        return CloseDate;
    }

    public void setCloseDate(Date closeDate) {
        CloseDate = closeDate;
    }

    public Date getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        ExpiryDate = expiryDate;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public Accounts getBank() {
        return accounts;
    }

    public void setBank(Accounts bank) {
        this.accounts = bank;
    }
}
