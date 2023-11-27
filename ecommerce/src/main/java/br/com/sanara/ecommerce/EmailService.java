package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Servi�o para detectar fraudes
 */
public class EmailService {

    public static void main(String[] args) {
        var consumer = new KafkaConsumer<String, String>(properties());

        //informa qual t�pico o consumidor vai querer "consumir" nome do t�pico
        consumer.subscribe(Collections.singletonList("ECOMMERCE_SEND_EMAIL"));

        //pergunta se tem msg dentro do t�pico no intervalo
        //coloca essa chamada em um la�o assim ele ficar� sempre escutando
        while (true) {
            var records = consumer.poll(Duration.ofMillis(100));

            if (!records.isEmpty()) {
                System.out.println("Encontrei " + records.count() + " registros");
                for (var record : records) {
                    System.out.println("--------------------------------------------");
                    System.out.println("Enviando email");
                    System.out.println(record.key());
                    System.out.println(record.value());
                    System.out.println(record.partition());
                    System.out.println(record.offset());

                    try {
                        //coloca um sleep pra aplica��o "dormir" por um tempo p/ simular a fraude 1 segundo
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //ignoring
                        e.printStackTrace();
                    }
                    System.out.println("Email enviado");
                }
            }
        }
    }

    private static Properties properties() {
        var properties = new Properties();

        //informando aonde o consumidor vai ficar "escutando"
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        //informa os deserializadores de bytes para String
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        //informa o id do grupo que ele ficar� escutando
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, EmailService.class.getSimpleName());

        return properties;
    }


}
