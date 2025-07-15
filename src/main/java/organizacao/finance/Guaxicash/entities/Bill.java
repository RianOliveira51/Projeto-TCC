package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String name;
    private Date CloseDate;
    private Date OpenDate;
    private Date PayDate;

    public Bill() {

    }

    public Bill(UUID uuid, String name, Date CloseDate, Date OpenDate, Date PayDate) {
        this.uuid = uuid;
        this.name = name;
        this.CloseDate = CloseDate;
        this.OpenDate = OpenDate;
        this.PayDate = PayDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCloseDate() {
        return CloseDate;
    }

    public void setCloseDate(Date closeDate) {
        CloseDate = closeDate;
    }

    public Date getOpenDate() {
        return OpenDate;
    }

    public void setOpenDate(Date openDate) {
        OpenDate = openDate;
    }

    public Date getPayDate() {
        return PayDate;
    }

    public void setPayDate(Date payDate) {
        PayDate = payDate;
    }
}
