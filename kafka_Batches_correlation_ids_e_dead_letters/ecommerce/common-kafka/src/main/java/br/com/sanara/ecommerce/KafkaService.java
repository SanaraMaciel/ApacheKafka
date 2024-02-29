package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

class KafkaService<T> implements Closeable {
    private final KafkaConsumer<String, Message<T>> consumer;
    private final ConsumerFunction parse;



    KafkaService(String groupId, String topic, ConsumerFunction<T> parse, Class<T> type, Map<String,String> properties) {
        this(parse, groupId, type, properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    KafkaService(String groupId, Pattern topic, ConsumerFunction<T> parse, Class<T> type, Map<String,String> properties) {
        this(parse, groupId, type, properties);
        consumer.subscribe(topic);
    }

    private KafkaService(ConsumerFunction parse, String groupId, Class<T> type, Map<String,String> properties) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(getProperties(type, groupId, properties));
    }

    void run() {
        while (true) {
            var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                System.out.println("Encontrei " + records.count() + " registros");
                for (var record : records) {
                    //tratando a exceção pq o Kafka consumer não pode lançar exceção
                    try {
                        parse.consume(record);
                    } catch (Exception e) {
                        //qualquer exceção porque não importa qual exceção der não vamos tratar,
                        //no caso só vamos pegar a próxima
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private Properties getProperties(Class<T> type, String groupId, Map<String,String> overrideProperties) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

        //configuração criada para deserializaçao do json em bytes
        //properties.setProperty(GsonDeserializer.TYPE_CONFIG, type.getName());

        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

        //sobrescreve todas as propriedades que já existem pelas que vc passou no mapa
        properties.putAll(overrideProperties);

        return properties;
    }


    @Override
    public void close() {
        consumer.close();
    }
}