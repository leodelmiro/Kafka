package br.com.leodelmiro.ecommerce.database;

import java.sql.*;

public class LocalDatabase {
    private final Connection connection;

    public LocalDatabase(String name) throws SQLException {
        String url = "jdbc:sqlite:target/" + name + ".db";
        this.connection = DriverManager.getConnection(url);
    }

    // yes, this is way too generic
    // according your database tool, avoid injection
    public void createIfNotExists(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            // be careful, the sql could be wrong, be really careful
            e.printStackTrace();
        }
    }

    public boolean update(String statement, String... params) throws SQLException {
        return prepareStatement(statement, params).execute();
    }

    public ResultSet query(String statement, String... params) throws SQLException {
        return prepareStatement(statement, params).executeQuery();
    }

    private PreparedStatement prepareStatement(String statement, String[] params) throws SQLException {
        var preparedStatement = connection.prepareStatement(statement);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setString(i + 1, params[i]);
        }
        return preparedStatement;
    }
}
