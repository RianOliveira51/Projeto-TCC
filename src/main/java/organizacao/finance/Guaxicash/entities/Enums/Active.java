package organizacao.finance.Guaxicash.entities.Enums;

public enum Active {

    ACTIVE(0),
    DISABLE(1);

    private final int code;

    Active(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Active fromCode(Integer code) {
        if (code == null) return null;
        for (Active b : values()) {
            if (b.code == code) return b;
        }
        throw new IllegalArgumentException("Código inválido para Active: " + code);
    }
}
