package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;
import organizacao.finance.Guaxicash.entities.Enums.Active;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "Reciphe")
public class Reciphe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Float value;
    private String description;
    private LocalDate dateRegistration;
    @ManyToOne
    @JoinColumn(name = "id_category")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "id_accounts")
    private Accounts accounts;
    private Active active = Active.ACTIVE;

    public Reciphe(){

    }

    public Reciphe(UUID uuid, Float value, String description,LocalDate dateRegistration, Category category, Accounts accounts, Active active) {
        this.uuid = uuid;
        this.value = value;
        this.description = description;
        this.dateRegistration = dateRegistration;
        this.category = category;
        this.accounts = accounts;
        this.active = active;
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

    public LocalDate getDateRegistration() {
        return dateRegistration;
    }

    public void setDateRegistration(LocalDate dateRegistration) {
        this.dateRegistration = dateRegistration;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Accounts getAccounts() {
        return accounts;
    }

    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }
}
