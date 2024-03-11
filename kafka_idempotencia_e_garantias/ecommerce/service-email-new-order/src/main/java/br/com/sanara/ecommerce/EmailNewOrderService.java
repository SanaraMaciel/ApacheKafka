package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.ConsumerService;
import br.com.sanara.ecommerce.consumer.ServiceRunner;
import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.ExecutionException;

public class EmailNewOrderService implements ConsumerService<Order> {

    //kafka dispatcher para enviar mensagens
    private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        new ServiceRunner(EmailNewOrderService::new).start(1);
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
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

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return EmailNewOrderService.class.getSimpleName();
    }


}