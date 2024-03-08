package br.com.sanara.ecommerce.consumer;

import br.com.sanara.ecommerce.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerService<T> {

    void parse(ConsumerRecord<String, Message<String>> record);

    String getTopic();

    String getConsumerGroup();
}
