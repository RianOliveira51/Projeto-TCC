package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;
import organizacao.finance.Guaxicash.entities.Enums.BillPay;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "Bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Double value;
    private BillPay status = BillPay.PENDING;
    private LocalDate  CloseDate;
    private LocalDate  OpenDate;
    private LocalDate  PayDate;

    @ManyToOne
    @JoinColumn(name = "creditCard_id")
    private CreditCard creditCard;

    public Bill(UUID uuid, String name, Double value, BillPay billPay, LocalDate CloseDate, LocalDate  OpenDate, LocalDate  PayDate, CreditCard creditCard) {
        this.uuid = uuid;
        this.value = value;
        this.status = billPay;
        this.CloseDate = CloseDate;
        this.OpenDate = OpenDate;
        this.PayDate = PayDate;
        this.creditCard = creditCard;
    }


    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public BillPay getStatus() {
        return status;
    }

    public void setStatus(BillPay status) {
        this.status = status;
    }

    public Bill() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDate  getCloseDate() {
        return CloseDate;
    }

    public void setCloseDate(LocalDate  closeDate) {
        CloseDate = closeDate;
    }

    public LocalDate  getOpenDate() {
        return OpenDate;
    }

    public void setOpenDate(LocalDate  openDate) {
        OpenDate = openDate;
    }

    public LocalDate  getPayDate() {
        return PayDate;
    }

    public void setPayDate(LocalDate  payDate) {
        PayDate = payDate;
    }
}
