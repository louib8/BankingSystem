package banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        String dbName = args[0];
        DBManager dbConn = new DBManager();
        dbConn.CreateDatabaseUnlessExists(dbName);

        CreateCardTable(dbConn);

        Scanner scanner = new Scanner(System.in);
        HashMap<String, Account> accounts = new HashMap<String, Account>();
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            var input = scanner.nextInt();

            if (input == 0) {
                exit = true;
                continue;
            }
            switch (input) {
                case 1:
                    var account = CreateAccount(dbConn);
                    /*if (accounts.containsKey(account.cardNumber)) {
                        while (accounts.containsKey(account.cardNumber)) {
                            account = CreateAccount(dbConn);
                        }
                    }*/
                    //accounts.put(account.cardNumber, account);
                    System.out.println();
                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    System.out.println(account.cardNumber);
                    System.out.println("Your card PIN:");
                    System.out.println(account.pin);
                    System.out.println();
                    break;
                case 2:
                    System.out.println();
                    System.out.println("Enter your card number:");
                    scanner.nextLine();
                    var inputCardNumber = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    var inputPinNumber = scanner.nextLine();
                    System.out.println();

                    if (accounts.containsKey(inputCardNumber) && accounts.get(inputCardNumber).pin.equals(inputPinNumber)) {
                        System.out.println("You have successfully logged in!");
                        System.out.println();
                        var condition = LoggedIn(accounts.get(inputCardNumber));
                        if (condition == 1) {
                            continue;
                        } else {
                            exit = true;
                        }
                    } else {
                        System.out.println("Wrong card number or PIN!");
                        System.out.println();
                    }

                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Unrecognised input");
            }
        }

        System.out.println("Bye!");
    }

    private static void CreateCardTable(DBManager dbConn) {
        String query = "CREATE TABLE IF NOT EXISTS" +
                "card (" +
                "id int NOT NULL," +
                "number varchar(20) NOT NULL," +
                "pin varchar(4) NOT NULL," +
                "balance int DEFAULT 0 NOT NULL;";
        dbConn.SQLExecute(query);
    }

    public static Account CreateAccount(DBManager dbConn) {
        Account acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4));
        while (Account.CheckIfAccountInDB(acct, dbConn)) {
            acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4));
        }
        Account.StoreAccountInSQL(acct, dbConn);
        return acct;
    }

    public static String GenerateCardNumber(int cardNumLength, String bin) {
        int binLength = bin.length();
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        cardNumber.append(bin);
        for (int i = binLength; i < cardNumLength - 1; i++) { //Minus 1 to allow for checksum value;
            cardNumber.append(random.nextInt(10));
        }

        cardNumber.append(GenerateCheckSum(cardNumber.toString()));

        return cardNumber.toString();
    }

    public static String GeneratePin(int pinLength) {
        Random random = new Random();
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < pinLength; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    public static int LoggedIn(Account account) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("1. Balance");
            System.out.println("2. Log out");
            System.out.println("0. Exit");
            var input = scanner.nextInt();
            System.out.println();

            switch (input) {
                case 1:
                    System.out.println("Balance: " + account.balance);
                    System.out.println();
                    break;

                case 2:
                    return 1;

                case 0:
                    return 0;

                default:
                    System.out.println("Unrecognised input");
                    System.out.println();
            }
        }
        return 0;
    }

    public static String GenerateCheckSum(String cardNumber) {
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
        } else {
            int checksum = 10 - (sum % 10);
            return String.valueOf(checksum);
        }
    }
}