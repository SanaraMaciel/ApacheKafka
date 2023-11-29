package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.regex.Pattern;

/**
 * nova classe refatorada
 */
public class LogService {

    //envia um pattern porque esse serviço fica escutando um grupo por regex
    public static void main(String[] args) {
        var logService = new LogService();
        try (var service = new KafkaService(LogService.class.getSimpleName(),
                Pattern.compile("ECOMMERCE.*") , logService::parse)) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, String> record) {
        System.out.println("------------------------------------------");
        System.out.println("LOG: " + record.topic());
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
    }

}
