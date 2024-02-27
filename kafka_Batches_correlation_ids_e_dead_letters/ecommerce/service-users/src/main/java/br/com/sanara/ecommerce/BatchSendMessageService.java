package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class BatchSendMessageService {

    private final Connection connection;

    BatchSendMessageService() throws SQLException {
        String url = "jdbc:sqlite:target/users_database.db";
        this.connection = DriverManager.getConnection(url);

        try {
            //criando uma tabela
            connection.createStatement().execute("create table Users (uuid varchar(200) primary key, " +
                    "email varchar(200))");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        var batchService = new BatchSendMessageService();
        try (var service = new KafkaService(BatchSendMessageService.class.getSimpleName(),
                "SEND_MESSAGE_TO_ALL_USERS", batchService::parse, String.class,
                Map.of(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()))) {
            service.run();
        }
    }

    private final KafkaDispatcher<User> userDispatcher = new KafkaDispatcher<>();

    private void parse(ConsumerRecord<String, String> record) throws ExecutionException, InterruptedException, SQLException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando novo batch");
        System.out.println("Topico: " + record.value());

        for (User user : getAllUsers()) {
            userDispatcher.send(record.value(), user.getUuid(), user);
        }

    }

    private List<User> getAllUsers() throws SQLException {
        var results = connection.prepareStatement("select uuid from Users ").executeQuery();
        List<User> users = new ArrayList<>();
        while (results.next()) {
            users.add(new User(results.getString(1)));
        }
        return users;
    }
}
