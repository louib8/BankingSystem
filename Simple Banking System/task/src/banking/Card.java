package banking;

import java.util.Random;

public class Card {
    private String cardNumber;
    private String pin;

    private void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    private void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public Card(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    public static String generateCardNumber(int cardNumLength, String bin) {
        int binLength = bin.length();
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        cardNumber.append(bin);
        for (int i = binLength; i < cardNumLength - 1; i++) { //Minus 1 to allow for checksum value;
            cardNumber.append(random.nextInt(10));
        }

        cardNumber.append(generateCheckSum(cardNumber.toString()));

        return cardNumber.toString();
    }

    public static String generatePin(int pinLength) {
        Random random = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < pinLength; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    public static String generateCheckSum(String cardNumber) {
        //There are three steps to generate a checksum using Luhn Algorithm

        int[] cardArray = new int[cardNumber.length()];
        int sum = 0;

        for (int i = 0; i < cardNumber.length(); i++) {
            cardArray[i] = Character.getNumericValue(cardNumber.charAt(i));
        }
        int steps = 0;
        while (steps < 3) {
            if (steps == 0) {
                for (int i = 0; i < cardNumber.length(); i++) {
                    cardArray[i] *= 2;
                    i++;
                }
            } else if (steps == 1) {
                for (int i = 0; i < cardNumber.length(); i++) {
                    if (cardArray[i] > 9) {
                        cardArray[i] -= 9;
                    }
                }
            } else if (steps == 2) {
                for (int i = 0; i < cardNumber.length(); i++) {
                    sum += cardArray[i];
                }
            }
            steps++;
        }

        if (sum % 10 == 0) {
            return "0";
        }
        int checksum = 10 - (sum % 10);
        return String.valueOf(checksum);
    }
}
