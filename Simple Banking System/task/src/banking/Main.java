package banking;

public class Main {
    public static void main(String[] args) {
        String sqlPath = DBManager.getSQLPath(args);

        DBManager dbConn = new DBManager(sqlPath);

        DBInit.initialiseDatabase(dbConn);

        BankUI.mainMenu(dbConn);
    }
}