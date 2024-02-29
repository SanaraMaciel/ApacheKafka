package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Servi�o para detectar fraudes refatorada
 */
public class FraudDetectorService {

    //kafka dispatcher para enviar mensagens
    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        var fraudService = new FraudDetectorService();
        try (var service = new KafkaService(FraudDetectorService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER", fraudService::parse, Order.class,
                Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando nova ordem, checando por fraude");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());

        var message = record.value();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //acessando a ordem
        var order = message.getPayload();
        //detectando uma fraude se ordem for maior que o valor 4500
        if (isFraude(order)) {
            System.out.println("A Order é uma fraude");
            orderDispatcher.send("ECOMMERCE_ORDER_REJECTED", order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
        } else {
            System.out.println("Ordem Aprovada: " + order);
            orderDispatcher.send("ECOMMERCE_ORDER_APPROVED", order.getEmail(),
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
        }

        System.out.println("Order processada");
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
