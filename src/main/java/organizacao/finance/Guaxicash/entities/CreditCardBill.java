package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "CreditCardBill")
public class CreditCardBill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Double value;
    private String description;
    private Date RegistrationDate;
    @ManyToOne
    @JoinColumn(name = "id_credit_card")
    private CreditCard creditCard;
    @ManyToOne
    @JoinColumn(name = "id_bill")
    private Bill bill;
    @ManyToOne
    @JoinColumn(name = "id_category")
    private Category category;

    public CreditCardBill() {

    }

    public CreditCardBill(UUID uuid, Double value, String description, Date registrationDate, CreditCard creditCard, Bill bill, Category category) {
        this.uuid = uuid;
        this.value = value;
        this.description = description;
        RegistrationDate = registrationDate;
        this.creditCard = creditCard;
        this.bill = bill;
        this.category = category;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
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
        RegistrationDate = registrationDate;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

