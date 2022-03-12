package banking;

public class CardTable {
    public int id;
    String number;
    String pin;
    int balance;

    public CardTable (int id, String number, String pin, int balance) {
        this.id = id;
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }
}
