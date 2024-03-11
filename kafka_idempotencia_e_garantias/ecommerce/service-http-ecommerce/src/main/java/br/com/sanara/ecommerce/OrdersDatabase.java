package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.database.LocalDatabase;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

public class OrdersDatabase implements Closeable {

    private final LocalDatabase database;

    OrdersDatabase() throws SQLException {
        //depois vamos ver a questão de salvar todos os dados
        this.database = new LocalDatabase("orders_database");
        database.createIfNotExists("create table Orders (uuid varchar(200) primary key)");
    }

    public boolean saveNew(Order order) throws SQLException {
        if (wasProcessed(order)) {
            return false;
        }
        database.update("INSERT INTO Orders (uuid) VALUES (?)", order.getOrderId());
        return true;
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from Orders where uuid = ? limit 1",
                order.getOrderId());
        return results.next();
    }

    @Override
    public void close() throws IOException {
        try {
            database.close();
        } catch (SQLException e) {
            throw new IOException(e.getMessage());
        }
    }
}
