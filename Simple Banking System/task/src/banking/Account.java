package banking;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Account {
    public String cardNumber;
    public String pin;
    public long balance;

    public Account(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = 0;
    }

    public static int StoreAccountInSQL (Account acct, DBManager dbConn) {
        int rowsAffected = 0;

        if(CheckIfAccountInDB(acct, dbConn)) {
            return -1;
        };

        String query = "INSERT INTO card (number, pin, balance)" +
                "VALUES (" + acct.cardNumber + ", " +
                acct.pin + ", " +
                acct.balance + ");";
        rowsAffected = dbConn.SQLExecuteUpdate(query);
        return rowsAffected;
    }

    public static Boolean CheckIfAccountInDB(Account acct, DBManager dbConn) {
        Boolean result = false;
        int numRows = 0;

        String query = "SELECT COUNT(number) " +
                "FROM card " +
                "WHERE number = " + acct.cardNumber + ";";

        try (ResultSet resultSet = dbConn.SQLExecuteQuery(query)) {
            while (resultSet.next()) {
                numRows = resultSet.getInt(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = numRows > 0 ? false : true;

        return result;
    }

    public static Boolean AuthenticateAccount(Account acct, DBManager dbConn) {
        String query = "SELECT * " +
                "FROM card " +
                "WHERE number = " + acct.cardNumber + "AND " +
                "pin = " + acct.pin + ";";
        try(ResultSet results = dbConn.SQLExecuteQuery(query)) {
            if (results.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
