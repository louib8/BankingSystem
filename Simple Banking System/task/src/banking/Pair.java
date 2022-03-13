package banking;

public class Pair<T, U> {
    private T firstValue;
    private U secondValue;

    public void setSecondValue (U secondValue) {
        this.secondValue = secondValue;
    }

    public U getSecondValue () {
        return this.secondValue;
    }

    public void setFirstValue (T firstValue) {
        this.firstValue = firstValue;
    }

    public T getFirstValue () {
        return this.firstValue;
    }

    public Pair(T firstValue, U secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }
}
