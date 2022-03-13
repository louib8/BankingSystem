package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private String url;

    private void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    SQLiteDataSource dataSource = new SQLiteDataSource();

    public DBManager (String URL) {
        this.url = URL;
        dataSource.setUrl(this.url);
    }

    public static String getSQLPath(String[] args) {
        int count = 0;
        String sqlPath = "jdbc:sqlite:";
        StringBuilder dbName = new StringBuilder();

        while (count < args.length) {
            if (args[count].equalsIgnoreCase("-fileName")) {
                dbName.append(args[count + 1]);
            }
            count++;
        }
        return sqlPath.concat(dbName.toString());
    }

    public Boolean sqlExecute(String query) {
        Boolean result = false;
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                result = statement.execute(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*public ResultSet sqlExecuteQuery(String query) {
        ResultSet results = null;
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                results = statement.executeQuery(query);
                return results;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }*/ //This doesn't work as the ResultSet is closed before it's returned, probably too much work to get around this.
    // Could potentially not end the transaction until later? return the result set and then when Resultset is received it has to be closed?

    public int sqlExecuteUpdate(String query) {
        int rowsAffected = -1;
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                rowsAffected = statement.executeUpdate(query);
                return rowsAffected;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }
}
