package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class DatabaseKafkaConsumer {

    private final KafkaConsumer<String, String> consumer;
    private final Database database; // Sua classe de conexão com o banco de dados

    public DatabaseKafkaConsumer(String topic, Database database) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "database-consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<>(props);
        this.database = database;
        this.consumer.subscribe(Arrays.asList(topic));
    }

    public void consume() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                database.save(record.value()); // Salva a mensagem no banco de dados
            }
        }
    }
}
