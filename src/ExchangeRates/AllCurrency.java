package ExchangeRates;

public enum AllCurrency {
    AUD(1),
    USD(2),
    CZK(3);

    private int i;

    AllCurrency(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }
}
