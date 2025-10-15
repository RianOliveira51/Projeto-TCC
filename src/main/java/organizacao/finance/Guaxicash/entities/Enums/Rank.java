package organizacao.finance.Guaxicash.entities.Enums;

public enum Rank {
    FERRO(0),
    BRONZE(500),
    OURO(2000),
    PLATINA(3500),
    DIAMANTE(5000);

    private final int minXp;
    Rank(int minXp) { this.minXp = minXp; }
    public int getMinXp() { return minXp; }

    public static Rank fromXp(int xp) {
        Rank r = FERRO;
        if (xp >= DIAMANTE.minXp) r = DIAMANTE;
        else if (xp >= PLATINA.minXp) r = PLATINA;
        else if (xp >= OURO.minXp) r = OURO;
        else if (xp >= BRONZE.minXp) r = BRONZE;
        return r;
    }
}
