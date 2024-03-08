package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.ConsumerService;
import br.com.sanara.ecommerce.consumer.ServiceRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;


public class EmailService implements ConsumerService<String> {

    public static void main(String[] args) {
        //chamando um callable 5 vezes - do 1 ao 5
        //IntStream.rangeClosed(1,5);

        /*cria um tread pool q vc determina o número de threads que vc quer executando em paralelo
        var pool = Executors.newFixedThreadPool(THREADS);
        var provider = new ServiceProvider(EmailService::new).start(THREADS);
        for(int i =0; i<= THREADS; i++){
            provider.call();
        }*/

        //uma outra maneira é fazer um serviço para que se chame uma vez e
        // ele fique encarregado do start
        new ServiceRunner(EmailService::new).start(5);

    }

    @Override
    public String getConsumerGroup() {
        return EmailService.class.getSimpleName();
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_SEND_EMAIL";
    }


    @Override
    public void parse(ConsumerRecord<String, Message<String>> record) {
        System.out.println("------------------------------------------");
        System.out.println("Enviando email");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignoring
            e.printStackTrace();
        }
        System.out.println("Email enviado");
    }

}
