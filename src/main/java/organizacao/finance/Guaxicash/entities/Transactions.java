package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Transactions")
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Float value;
    private String description;
    private Date RegistrationDate;
    @ManyToOne
    @JoinColumn(name = "id_ foraccounts")
    private Accounts foraccounts;
    @ManyToOne
    @JoinColumn(name = "id_accounts")
    private Accounts accounts;
    @ManyToOne
    @JoinColumn(name = "id_category")
    private Category category;


    public Transactions() {

    }

    public Transactions(UUID uuid, Float value, String description, Date RegistrationDate, Accounts foraccounts, Accounts accounts, Category category) {
        this.uuid = uuid;
        this.value = value;
        this.description = description;
        this.RegistrationDate = RegistrationDate;
        this.foraccounts = foraccounts;
        this.accounts = accounts;
        this.category = category;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getRegistrationDate() {
        return RegistrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.RegistrationDate = registrationDate;
    }

    public Accounts getForaccounts() {
        return foraccounts;
    }

    public void setForaccounts(Accounts foraccounts) {
        this.foraccounts = foraccounts;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
