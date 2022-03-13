package banking;

import java.util.Scanner;

import static banking.Account.createAccount;

public class BankUI {
    public static void mainMenu(DBManager dbConn) {
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
                    var account = createAccount(dbConn);
                    System.out.println();
                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    System.out.println(account.getCard().getCardNumber());
                    System.out.println("Your card PIN:");
                    System.out.println(account.getCard().getPin());
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

                    Account loginAccount = new Account(inputCardNumber, inputPinNumber, dbConn);

                    if (Account.authenticateAccount(loginAccount)) {
                        System.out.println("You have successfully logged in!");
                        System.out.println();
                        var condition = loggedInMenu(loginAccount);
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

    public static int loggedInMenu(Account account) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            var input = scanner.nextInt();
            System.out.println();

            switch (input) {
                case 1:
                    System.out.println("Balance: " + account.getBalance());
                    System.out.println();
                    break;

                case 2:
                    System.out.println("Enter the amount you would like to deposit: ");
                    int deposit = scanner.nextInt();
                    System.out.println();
                    Boolean attemptDeposit = account.addIncomeToAccount(deposit);
                    if (attemptDeposit) {
                        System.out.println("Successfully deposited: " + deposit);
                        System.out.println("Account balance now: " + account.getBalance());
                        System.out.println();
                    } else {
                        System.out.println("Deposit attempt failed.");
                        System.out.println();
                    }
                    System.out.println();
                    break;

                case 3:
                    System.out.println("Enter the account number you want to transfer to: ");
                    scanner.nextLine();
                    String destinationAccountNumber = scanner.nextLine();
                    System.out.println();
                    var validAcct = account.validateAccount(destinationAccountNumber, account);
                    if (!validAcct.getFirstValue()) {
                        System.out.println(validAcct.getSecondValue());
                        System.out.println();
                        break;
                    }

                    System.out.println("Please enter the amount you would like to transfer: ");
                    var transferAmount = scanner.nextInt();

                    var validTransfer = account.validateTransfer(transferAmount, destinationAccountNumber);
                    if (!validTransfer.getFirstValue()) {
                        System.out.println(validTransfer.getSecondValue());
                        System.out.println();
                        break;
                    }

                    if (account.transferToAccount(destinationAccountNumber, transferAmount)) {
                        System.out.println("Successfully transferred: " + transferAmount);
                        System.out.println("From Account Number: " + account.getCard().getCardNumber());
                        System.out.println("To Account Number: " + destinationAccountNumber);
                        System.out.println();
                    } else {
                        System.out.println("Transaction Failed, no money was withdrawn or deposited from either account.");
                        System.out.println();
                    }
                    break;

                case 4:
                    if (account.closeAccount()) {
                        System.out.println("Account successfully closed");
                        System.out.println();
                        return 1;
                    } else {
                        System.out.println("There was an issue closing the account, the account has not been closed");
                        System.out.println();
                        break;
                    }

                case 5:
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
