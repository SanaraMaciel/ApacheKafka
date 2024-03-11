package br.com.sanara.ecommerce.database;

import java.sql.*;
import java.util.UUID;

public class LocalDatabase {

    private final Connection connection;

    public LocalDatabase(String name) throws SQLException {
        String url = "jdbc:sqlite:target/" + name + ".db";
        connection = DriverManager.getConnection(url);
    }

    /**
     * cria uma tabela se ela não existir
     *
     * @param sql
     */
    public void createIfNotExists(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * faz um update no banco conforme parametro e query
     * @param statement
     * @param params
     * @throws SQLException
     */
    public void update(String statement, String... params) throws SQLException {
        var preparedStatement = getPreparedStatement(statement, params);
        preparedStatement.execute();
    }

    /**
     * faz uma busca no banco conforme query e parametro
     * @param query
     * @param params
     * @return
     * @throws SQLException
     */
    public ResultSet query(String query, String... params) throws SQLException {
        var preparedStatement = getPreparedStatement(query, params);
        return preparedStatement.executeQuery();
    }

    private PreparedStatement getPreparedStatement(String statement, String[] params) throws SQLException {
        var preparedStatement = connection.prepareStatement(statement);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setString(i + 1, params[i]);
        }
        return preparedStatement;
    }
}
