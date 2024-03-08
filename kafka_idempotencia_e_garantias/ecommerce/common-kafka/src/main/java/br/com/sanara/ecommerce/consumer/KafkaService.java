package br.com.sanara.ecommerce.consumer;

import br.com.sanara.ecommerce.Message;
import br.com.sanara.ecommerce.dispatcher.GsonSerializer;
import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class KafkaService<T> implements Closeable {
    private final KafkaConsumer<String, Message<T>> consumer;
    private final ConsumerFunction parse;

    public KafkaService(String groupId, String topic, ConsumerFunction<T> parse, Map<String, String> properties) {
        this(parse, groupId, properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public KafkaService(String groupId, Pattern topic, ConsumerFunction<T> parse, Map<String, String> properties) {
        this(parse, groupId, properties);
        consumer.subscribe(topic);
    }

    private KafkaService(ConsumerFunction parse, String groupId, Map<String, String> properties) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(getProperties(groupId, properties));
    }

    public void run() throws ExecutionException, InterruptedException {
        try (var deadLetterDispatcher = new KafkaDispatcher<>()) {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(100));
                if (!records.isEmpty()) {
                    System.out.println("Encontrei " + records.count() + " registros");
                    for (var record : records) {
                        //tratando a exceção pq o Kafka consumer não pode lançar exceção
                        try {
                            parse.consume(record);
                        } catch (Exception e) {
                            //logando as falhas com topico deadletter -- batch sincrono
                            e.printStackTrace();
                            var message = record.value();
                            deadLetterDispatcher.send("ECOMMERCE_DEADLETTER", message.getId().toString(),
                                    message.getId().continueWith("DeadLetter"),
                                    new GsonSerializer().serialize("", message));
                        }
                    }
                }
            }
        }
    }

    private Properties getProperties(String groupId, Map<String, String> overrideProperties) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

        //configuração criada para deserializaçao do json em bytes
        //properties.setProperty(GsonDeserializer.TYPE_CONFIG, type.getName());

        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        //configura o offset qdo não tem o offset inicial inndicando
        // da onde ele deve começar, e o que fazer qdo não tem o inicial ou qdo os dados foram apagados
        //latest é o mais recente
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        //sobrescreve todas as propriedades que já existem pelas que vc passou no mapa
        properties.putAll(overrideProperties);

        return properties;
    }


    @Override
    public void close() {
        consumer.close();
    }
}