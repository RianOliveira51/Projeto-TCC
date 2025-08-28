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
    private LocalDate closeDate;
    private LocalDate openDate;
    private LocalDate payDate;

    @ManyToOne
    @JoinColumn(name = "creditCard_id")
    private CreditCard creditCard;

    public Bill(UUID uuid, String name, Double value, BillPay billPay, LocalDate closeDate, LocalDate  openDate, LocalDate  PayDate, CreditCard creditCard) {
        this.uuid = uuid;
        this.value = value;
        this.status = billPay;
        this.closeDate = closeDate;
        this.openDate = openDate;
        this.payDate = PayDate;
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
        return closeDate;
    }

    public void setCloseDate(LocalDate  closeDate) {
        closeDate = closeDate;
    }

    public LocalDate  getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate  openDate) {
        openDate = openDate;
    }

    public LocalDate  getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDate  payDate) {
        payDate = payDate;
    }
}
