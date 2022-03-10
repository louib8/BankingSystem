package banking;

public class DBInit {
    public static void CreateCardTable(DBManager dbConn) {
        String query = "CREATE TABLE IF NOT EXISTS " +
                "card (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "number varchar(20) NOT NULL, " +
                "pin varchar(4) NOT NULL, " +
                "balance int DEFAULT 0 NOT NULL);";
        dbConn.SQLExecute(query);
    }

    public static void CreateDatabaseUnlessExists(String databaseName, DBManager dbConn) {
        dbConn.SQLExecute(databaseName);
    }
}
