package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.ConsumerService;
import br.com.sanara.ecommerce.consumer.KafkaService;
import br.com.sanara.ecommerce.consumer.ServiceRunner;
import br.com.sanara.ecommerce.database.LocalDatabase;
import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Servico para detectar fraudes refatorada
 */
public class FraudDetectorService implements ConsumerService<Order> {

    private final LocalDatabase database;

    FraudDetectorService() throws SQLException {
        this.database = new LocalDatabase("frauds_database");
        database.createIfNotExists("create table Orders (uuid varchar(200) primary key, " +
                "is_fraud boolean)");
    }

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        new ServiceRunner(FraudDetectorService::new).start(1);
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return FraudDetectorService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException, SQLException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando nova ordem, checando por fraude");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());

        var message = record.value();
        var order = message.getPayload();
        //só processa se a order é fraude caso ela não foi processada ainda
        if (wasProcessed(order)) {
            System.out.println("A Ordem " + order.getOrderId() + " já foi processada!");
            return;
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //detectando uma fraude se ordem for maior que o valor 4500
        if (isFraude(order)) {
            database.update("INSERT INTO Orders (uuid,is_fraud) VALUES (?, true)", order.getOrderId());

            System.out.println("A Order é uma fraude");
            orderDispatcher.send("ECOMMERCE_ORDER_REJECTED", order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
        } else {
            database.update("INSERT INTO Orders (uuid,is_fraud) VALUES (?, false)", order.getOrderId());
            System.out.println("Ordem Aprovada: " + order);
            orderDispatcher.send("ECOMMERCE_ORDER_APPROVED", order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
        }

        System.out.println("Order processada");
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from Orders where uuid = ? limit 1",
                order.getOrderId());
        return results.next();
    }

    /**
     * verifica se a order recebida é uma fraude comparando ela com o valor 4500,00
     *
     * @param order
     * @return um booleano que informa se é fraude (true) ou não (false)
     */
    private static boolean isFraude(Order order) {
        return order.getAmount().compareTo(new BigDecimal("4500")) >= 0;
    }

}
