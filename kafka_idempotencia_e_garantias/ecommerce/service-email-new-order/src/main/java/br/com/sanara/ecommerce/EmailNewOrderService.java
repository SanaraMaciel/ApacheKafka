package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import br.com.sanara.ecommerce.consumer.KafkaService;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EmailNewOrderService {

    //kafka dispatcher para enviar mensagens
    private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var emailNewOrderService = new EmailNewOrderService();
        try (var service = new KafkaService(EmailNewOrderService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER", emailNewOrderService::parse, Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando nova ordem, preparando email");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());

        var message = record.value();
        var id = message.getId().continueWith(EmailNewOrderService.class.getSimpleName());
        var order = record.value().getPayload();
        var emailCode = "Obrigado por comprar conosco estamos processando sua compra!";
        emailDispatcher.send("ECOMMERCE_SEND_EMAIL", order.getEmail(), id, emailCode);

    }


}