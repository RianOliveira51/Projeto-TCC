package organizacao.finance.Guaxicash.entities.Enums;

public enum Rank {
    FERRO(0),
    BRONZE(500),
    OURO(1000),
    PLATINA(2000),
    DIAMANTE(3500);

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
