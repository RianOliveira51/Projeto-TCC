package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Reciphe")
public class Reciphe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Float value;
    private String description;
    private Date dateRegistration;
    private Date datePay;
    @ManyToOne
    @JoinColumn(name = "id_category")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "id_accounts")
    private Accounts accounts;

    public Reciphe(){

    }

    public Reciphe(UUID uuid, Float value, String description,Date dateRegistration,Date datePay,  Category category, Accounts accounts) {
        this.uuid = uuid;
        this.value = value;
        this.description = description;
        this.dateRegistration = dateRegistration;
        this.datePay = datePay;
        this.category = category;
        this.accounts = accounts;
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

    public Date getDateRegistration() {
        return dateRegistration;
    }

    public void setDateRegistration(Date dateRegistration) {
        this.dateRegistration = dateRegistration;
    }

    public Date getDatePay() {
        return datePay;
    }

    public void setDatePay(Date datePay) {
        this.datePay = datePay;
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
}
