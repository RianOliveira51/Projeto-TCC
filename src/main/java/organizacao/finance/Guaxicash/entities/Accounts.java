package organizacao.finance.Guaxicash.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Accounts")
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String name;
    private Float balance;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private Type type;

    public Accounts(){

    }

    public Accounts(UUID uuid, String name, Float balance, Bank bank, Type type){
        this.uuid = uuid;
        this.name = name;
        this.balance = balance;
        this.bank = bank;
        this.type = type;
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

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
