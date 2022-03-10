package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private String url;
    SQLiteDataSource dataSource = new SQLiteDataSource();

    public DBManager() {
        dataSource.setUrl(url);
    }

    public DBManager (String URL) {
        this.url = URL;
        dataSource.setUrl(url);
    }

    public Boolean SQLExecute(String query) {
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

    public ResultSet SQLExecuteQuery(String query) {
        ResultSet results = null;
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                results = statement.executeQuery(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public int SQLExecuteUpdate(String query) {
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
