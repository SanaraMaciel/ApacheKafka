package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.ConsumerService;
import br.com.sanara.ecommerce.consumer.KafkaService;
import br.com.sanara.ecommerce.consumer.ServiceRunner;
import br.com.sanara.ecommerce.database.LocalDatabase;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Serviço para detectar novos usuários ao fazer uma order de compra -- refatorada
 */
public class CreateUserService implements ConsumerService<Order> {

    private final LocalDatabase database;

    CreateUserService() throws SQLException {
        this.database = new LocalDatabase("users_database");
        database.createIfNotExists("create table Users (uuid varchar(200) primary key, " +
                "email varchar(200))");
    }


    public static void main(String[] args) {
        new ServiceRunner(CreateUserService::new).start(1);
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws SQLException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando nova ordem, checando por novo usuário");
        System.out.println(record.value());

        //verifica e insere o usuário no banco
        var order = record.value().getPayload();
        if (isNewUser(order.getEmail())) {
            insertNewUser(order.getEmail());
        }

    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return CreateUserService.class.getSimpleName();
    }

    private void insertNewUser(String email) throws SQLException {
        var uuid = UUID.randomUUID().toString();
        database.update("insert into Users ( uuid, email) " +
                "values(?, ?)", uuid, email);

        System.out.println("Usuário de uuid: " + uuid + "  e email " + email + "adicionado");
    }

    private boolean isNewUser(String email) throws SQLException {
        var results = database.query("select uuid from Users " +
                "where email = ? limit 1", email);

        return !results.next();
    }

}
