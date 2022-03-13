package banking;

import java.sql.*;

import static banking.Card.*;

public class Account {
    public Card card;
    private long balance;
    private DBManager dbConn;

    public Boolean closeDBConn() {
        try {
            if (!dbConn.dataSource.getConnection().isClosed()) {
                dbConn.dataSource.getConnection().close();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getBalance() {
        setBalance();
        return balance;
    }

    private void setBalance() {
        this.balance = GetAccountBalanceFromDB(this, dbConn);
    }

    public Account(String cardNumber, String pin, DBManager dbConn) {
        this.card = new Card(cardNumber, pin);
        this.balance = 0;
        this.dbConn = dbConn;
    }


    public Pair<Boolean, String> ValidateAccount(String destinationAccountNumber, Account account) {
        if (destinationAccountNumber.length() < 3) return new Pair<Boolean, String>(false, "Invalid destination card number");
        String cardNumWithoutCheckSum = destinationAccountNumber.substring(0, destinationAccountNumber.length() - 1);
        String originalCheckSum = destinationAccountNumber.substring(destinationAccountNumber.length() - 1, destinationAccountNumber.length());

        if (!GenerateCheckSum(cardNumWithoutCheckSum).equalsIgnoreCase(originalCheckSum)) {
            return new Pair<Boolean, String>(false, "Probably you made a mistake in the card number. Please try again!");
        }

        if (!Account.CheckIfAccountInDB(new Account(destinationAccountNumber, "0000", account.dbConn))) {
            return new Pair<Boolean, String>(false, "Such a card does not exist.");
        }

        return new Pair<Boolean, String>(true, "");
    }

    private long GetAccountBalanceFromDB(Account account, DBManager dbConn) {
        long dbBalance = 0;

        String query = "SELECT balance FROM card WHERE number = ? AND pin = ?";

        try (Connection con = dbConn.dataSource.getConnection()) {
            try (PreparedStatement getBalance = con.prepareStatement(query)) {
                getBalance.setString(1, account.card.cardNumber);
                getBalance.setString(2, account.card.pin);
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

    public static int StoreAccountInSQL (Account acct) {
        int rowsAffected = 0;

        if(CheckIfAccountInDB(acct)) {
            return -1;
        }

        String query = "INSERT INTO card (number, pin, balance) " +
                "VALUES ( '" + acct.card.cardNumber + "', '" +
                acct.card.pin + "', " +
                acct.balance + ");";
        rowsAffected = acct.dbConn.SQLExecuteUpdate(query);
        return rowsAffected;
    }

    public static Boolean CheckIfAccountInDB(Account acct) {
        Boolean result = false;
        int numRows = 0;

        String query = "SELECT COUNT(number) " +
                "FROM card " +
                "WHERE number = '" + acct.card.cardNumber + "';";


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

    public static Boolean AuthenticateAccount(Account acct) {
        String query = "SELECT * " +
                "FROM card " +
                "WHERE number = '" + acct.card.cardNumber + "' AND " +
                "pin = '" + acct.card.pin + "';";

        try (Connection con = acct.dbConn.dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try(ResultSet results = statement.executeQuery(query)) {
                    if (results.isBeforeFirst()) {
                        acct.balance = acct.getBalance();
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

    public static Account CreateAccount(DBManager dbConn) {
        Account acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4), dbConn);
        while (Account.CheckIfAccountInDB(acct)) {
            acct = new Account(GenerateCardNumber(16, "400000"), GeneratePin(4), dbConn);
        }
        Account.StoreAccountInSQL(acct);
        return acct;
    }

    public Boolean AddIncomeToAccount(int deposit) {
        String query = "UPDATE card SET balance = balance + ? WHERE number = ? AND pin = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint savepoint = con.setSavepoint();
            try (PreparedStatement updateBalance = con.prepareStatement(query)) {
                updateBalance.setInt(1, deposit);
                updateBalance.setString(2, this.card.cardNumber);
                updateBalance.setString(3, this.card.pin);
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

    public Pair<Boolean, String> ValidateTransfer(int transferAmount, String destinationAccountNumber) {
        if (this.getBalance() < transferAmount) {
            return new Pair<Boolean, String>(false, "Not enough money!");
        }

        if (this.card.cardNumber.equalsIgnoreCase(destinationAccountNumber)) {
            return new Pair<Boolean, String>(false, "You can't transfer money to the same account!");
        }
        return new Pair<Boolean, String>(true, "");
    }

    public Boolean TransferToAccount(String destinationAccountNumber, int transferAmount) {
        String withdrawQuery = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String depositQuery = "UPDATE card SET balance = balance + ? WHERE number = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint beforeWithdraw = con.setSavepoint();
            try (PreparedStatement withdraw = con.prepareStatement(withdrawQuery);
                PreparedStatement deposit = con.prepareStatement(depositQuery)) {

                withdraw.setInt(1, transferAmount);
                withdraw.setString(2, this.card.cardNumber);
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

    public boolean CloseAccount() {
        String deleteQuery = "DELETE FROM card WHERE number = ? AND pin = ?";

        try (Connection con = this.dbConn.dataSource.getConnection()) {
            con.setAutoCommit(false);
            Savepoint savepoint = con.setSavepoint();
            try (PreparedStatement deleteAccount = con.prepareStatement(deleteQuery)) {
                deleteAccount.setString(1, this.card.cardNumber);
                deleteAccount.setString(2, this.card.pin);
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
