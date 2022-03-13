package banking;

import java.sql.*;

import static banking.Card.*;

public class Account {
    private Card card;
    private DBManager dbConn;

    public Card getCard() {
        return card;
    }

    private void setCard(Card card) {
        this.card = card;
    }

    public long getBalance() {
        return getAccountBalanceFromDB(this);
    }

    public DBManager getDbConn() {
        return dbConn;
    }

    private void setDbConn(DBManager dbConn) {
        this.dbConn = dbConn;
    }

    public Account(String cardNumber, String pin, DBManager dbConn) {
        this.card = new Card(cardNumber, pin);
        this.dbConn = dbConn;
    }


    public Pair<Boolean, String> validateAccount(String destinationAccountNumber, Account account) {
        if (destinationAccountNumber.length() < 3) return new Pair<>(false, "Invalid destination card number");
        String cardNumWithoutCheckSum = destinationAccountNumber.substring(0, destinationAccountNumber.length() - 1);
        String originalCheckSum = destinationAccountNumber.substring(destinationAccountNumber.length() - 1, destinationAccountNumber.length());

        if (!generateCheckSum(cardNumWithoutCheckSum).equalsIgnoreCase(originalCheckSum)) {
            return new Pair<>(false, "Probably you made a mistake in the card number. Please try again!");
        }

        if (!Account.checkIfAccountInDB(new Account(destinationAccountNumber, "0000", account.dbConn))) {
            return new Pair<>(false, "Such a card does not exist.");
        }

        return new Pair<>(true, "");
    }

    private long getAccountBalanceFromDB(Account account) {
        long dbBalance = 0;

        String query = "SELECT balance FROM card WHERE number = ? AND pin = ?";

        try (Connection con = account.getDbConn().dataSource.getConnection()) {
            try (PreparedStatement getBalance = con.prepareStatement(query)) {
                getBalance.setString(1, account.card.getCardNumber());
                getBalance.setString(2, account.card.getPin());
                try (ResultSet resultSet = getBalance.executeQuery()) {
                    dbBalance = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dbBalance;
    }

    public static int storeAccountInSQL(Account acct) {
        int rowsAffected = 0;

        if(checkIfAccountInDB(acct)) {
            return -1;
        }

        String query = "INSERT INTO card (number, pin, balance) " +
                "VALUES ( '" + acct.card.getCardNumber() + "', '" +
                acct.card.getPin() + "', " +
                acct.getBalance() + ");";
        rowsAffected = acct.dbConn.sqlExecuteUpdate(query);
        return rowsAffected;
    }

    public static Boolean checkIfAccountInDB(Account acct) {
        Boolean result = false;
        int numRows = 0;

        String query = "SELECT COUNT(number) " +
                "FROM card " +
                "WHERE number = '" + acct.card.getCardNumber() + "';";


        try (Connection con = acct.dbConn.dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    numRows = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = numRows > 0;

        return result;
    }

    public static Boolean authenticateAccount(Account acct) {
        String query = "SELECT * " +
                "FROM card " +
                "WHERE number = '" + acct.card.getCardNumber() + "' AND " +
                "pin = '" + acct.card.getPin() + "';";

        try (Connection con = acct.dbConn.dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try(ResultSet results = statement.executeQuery(query)) {
                    if (results.isBeforeFirst()) {
                        return true;
                    }
                    return false;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Account createAccount(DBManager dbConn) {
        Account acct = new Account(generateCardNumber(16, "400000"), generatePin(4), dbConn);
        while (Account.checkIfAccountInDB(acct)) {
            acct = new Account(generateCardNumber(16, "400000"), generatePin(4), dbConn);
        }
        Account.storeAccountInSQL(acct);
        return acct;
    }

    public Boolean addIncomeToAccount(int deposit) {
        String query = "UPDATE card SET balance = balance + ? WHERE number = ? AND pin = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint savepoint = con.setSavepoint();
            try (PreparedStatement updateBalance = con.prepareStatement(query)) {
                updateBalance.setInt(1, deposit);
                updateBalance.setString(2, this.card.getCardNumber());
                updateBalance.setString(3, this.card.getPin());

                int result = updateBalance.executeUpdate();

                con.commit();

                return result > 0;
            } catch (SQLException e) {
                con.rollback(savepoint);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Pair<Boolean, String> validateTransfer(int transferAmount, String destinationAccountNumber) {
        if (this.getBalance() < transferAmount) {
            return new Pair<>(false, "Not enough money!");
        }

        if (this.card.getCardNumber().equalsIgnoreCase(destinationAccountNumber)) {
            return new Pair<>(false, "You can't transfer money to the same account!");
        }
        return new Pair<>(true, "");
    }

    public Boolean transferToAccount(String destinationAccountNumber, int transferAmount) {
        String withdrawQuery = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String depositQuery = "UPDATE card SET balance = balance + ? WHERE number = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint beforeWithdraw = con.setSavepoint();
            try (PreparedStatement withdraw = con.prepareStatement(withdrawQuery);
                PreparedStatement deposit = con.prepareStatement(depositQuery)) {

                withdraw.setInt(1, transferAmount);
                withdraw.setString(2, this.card.getCardNumber());
                int withdrawResult = withdraw.executeUpdate();

                deposit.setInt(1, transferAmount);
                deposit.setString(2, destinationAccountNumber);
                int depositResult = deposit.executeUpdate();

                con.commit();
                return depositResult > 0 && withdrawResult > 0;
            } catch (SQLException e) {
                con.rollback(beforeWithdraw);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean closeAccount() {
        String deleteQuery = "DELETE FROM card WHERE number = ? AND pin = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint savepoint = con.setSavepoint();
            try (PreparedStatement deleteAccount = con.prepareStatement(deleteQuery)) {
                deleteAccount.setString(1, this.card.getCardNumber());
                deleteAccount.setString(2, this.card.getPin());

                int result = deleteAccount.executeUpdate();

                con.commit();

                return result > 0;
            } catch (SQLException e) {
                con.rollback(savepoint);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
