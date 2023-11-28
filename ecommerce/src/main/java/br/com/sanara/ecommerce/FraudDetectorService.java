package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Serviço para detectar fraudes
 */
public class FraudDetectorService {

    public static void main(String[] args) {
        var consumer = new KafkaConsumer<String, String>(properties());

        //informa qual tópico o consumidor vai querer "consumir" nome do tópico
        consumer.subscribe(Collections.singletonList("ECOMMERCE_NEW_ORDER"));

        //pergunta se tem msg dentro do tópico no intervalo
        //coloca essa chamada em um laço assim ele ficará sempre escutando
        while (true) {
            var records = consumer.poll(Duration.ofMillis(100));

            if (!records.isEmpty()) {
                System.out.println("Encontrei " + records.count() + " registros");
                for (var record : records) {
                    System.out.println("--------------------------------------------");
                    System.out.println("Processando nova ordem, checando por fraude");
                    System.out.println(record.key());
                    System.out.println(record.value());
                    System.out.println(record.partition());
                    System.out.println(record.offset());

                    try {
                        //coloca um sleep pra aplicação "dormir" por um tempo p/ simular a fraude 5 segundos
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        //ignoring
                        e.printStackTrace();
                    }
                    System.out.println("Order processada");
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

        //informa o id do grupo que ele ficará escutando
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, FraudDetectorService.class.getSimpleName());

        //informando um id para o seu consumidor manualmente
        //properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "ip da maquina");

        return properties;
    }


}
