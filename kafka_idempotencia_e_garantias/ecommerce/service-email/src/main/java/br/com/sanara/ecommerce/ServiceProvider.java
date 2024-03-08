package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.KafkaService;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ServiceProvider {
    public <T> void run(ServiceFactory<T> factory) throws ExecutionException, InterruptedException {
        //cria a fábrica para disparar o dispatcher
        var emailService = factory.create();
        try (var service = new KafkaService(emailService.getConsumerGroup(), emailService.getTopic(),
                emailService::parse, Map.of())) {
            service.run();
        }
    }
}
