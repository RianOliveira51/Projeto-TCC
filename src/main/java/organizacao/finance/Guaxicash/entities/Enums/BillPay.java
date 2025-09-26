package organizacao.finance.Guaxicash.entities.Enums;

public enum BillPay {

    CLOSE(0),
    PAID(1),
    FUTURE_BILLS(2),
    OPEN(3);

    private final int code;

    BillPay(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BillPay fromCode(Integer code) {
        if (code == null) return null;
        for (BillPay b : values()) {
            if (b.code == code) return b;
        }
        throw new IllegalArgumentException("Código inválido para BillPay: " + code);
    }
}
