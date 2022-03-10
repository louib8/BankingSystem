package banking;

import java.sql.ResultSet;
import java.sql.SQLException;

import static banking.Card.*;

public class Account {
    public Card card;
    public long balance;

    public Account(String cardNumber, String pin) {
        this.card = new Card(cardNumber, pin);
        this.balance = 0;
    }

    public static int StoreAccountInSQL (Account acct, DBManager dbConn) {
        int rowsAffected = 0;

        if(CheckIfAccountInDB(acct, dbConn)) {
            return -1;
        };

        String query = "INSERT INTO card (number, pin, balance) " +
                "VALUES (" + acct.card.cardNumber + ", " +
                acct.card.pin + ", " +
                acct.balance + ");";
        rowsAffected = dbConn.SQLExecuteUpdate(query);
        return rowsAffected;
    }

    public static Boolean CheckIfAccountInDB(Account acct, DBManager dbConn) {
        Boolean result = false;
        int numRows = 0;

        String query = "SELECT COUNT(number) " +
                "FROM card " +
                "WHERE number = " + acct.card.cardNumber + ";";

        try (ResultSet resultSet = dbConn.SQLExecuteQuery(query)) {
            while (resultSet.next()) {
                numRows = resultSet.getInt(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = numRows > 0 ? true : false;

        return result;
    }

    public static Boolean AuthenticateAccount(Account acct, DBManager dbConn) {
        String query = "SELECT * " +
                "FROM card " +
                "WHERE number = " + acct.card.cardNumber + " AND " +
                "pin = " + acct.card.pin + ";";
        try(ResultSet results = dbConn.SQLExecuteQuery(query)) {
            acct.balance = results.getInt("balance");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Account CreateAccount(DBManager dbConn) {
        Account acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4));
        while (Account.CheckIfAccountInDB(acct, dbConn)) {
            acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4));
        }
        Account.StoreAccountInSQL(acct, dbConn);
        return acct;
    }
}
