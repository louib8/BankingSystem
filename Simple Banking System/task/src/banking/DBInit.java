package banking;

public class DBInit {
    public static void initialiseDatabase(DBManager dbConn) {
        createCardTable(dbConn);
    }
    public static void createCardTable(DBManager dbConn) {
        String query = "CREATE TABLE IF NOT EXISTS " +
                "card (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "number varchar(20) NOT NULL, " +
                "pin varchar(4) NOT NULL, " +
                "balance int DEFAULT 0 NOT NULL);";
        dbConn.sqlExecute(query);
    }
}
