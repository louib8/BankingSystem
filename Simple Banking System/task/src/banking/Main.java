package banking;

import java.util.HashMap;
import java.util.Scanner;

import static banking.Account.CreateAccount;
import static banking.DBInit.CreateCardTable;
import static banking.DBInit.CreateDatabaseUnlessExists;
import static banking.FileSystem.CheckForFile;

public class Main {
    public static void main(String[] args) {
        int count = 0;
        String sqlPath = "jdbc:sqlite:C:/Users/saphi/OneDrive - Cromarty/Desktop/SQLite/";
        StringBuilder dbName = new StringBuilder();

        while (count < args.length) {
            if (args[count].equalsIgnoreCase("-fileName")) {
                dbName.append(args[count + 1]);
            }
            count++;
        }

        DBManager dbConn = new DBManager(sqlPath.concat(dbName.toString()));

        if (CheckForFile(sqlPath + dbName.toString())) {
            CreateCardTable(dbConn);
        } else {
            int index = dbName.indexOf(".");
            String db = dbName.toString().substring(0, index);
            //CreateDatabaseUnlessExists(db, dbConn);
            CreateCardTable(dbConn);
        }

        Scanner scanner = new Scanner(System.in);
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
                    System.out.println();
                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    System.out.println(account.card.cardNumber);
                    System.out.println("Your card PIN:");
                    System.out.println(account.card.pin);
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

                    Account loginAccount = new Account(inputCardNumber, inputPinNumber);

                    if (Account.AuthenticateAccount(loginAccount, dbConn)) {
                        System.out.println("You have successfully logged in!");
                        System.out.println();
                        var condition = LoggedIn(loginAccount);
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
}