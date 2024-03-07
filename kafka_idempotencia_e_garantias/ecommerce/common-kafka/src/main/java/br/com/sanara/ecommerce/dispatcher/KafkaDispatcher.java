package br.com.sanara.ecommerce.dispatcher;

import br.com.sanara.ecommerce.CorrelationId;
import br.com.sanara.ecommerce.Message;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaDispatcher<T> implements Closeable {

    private final KafkaProducer<String, Message<T>> producer;

    public KafkaDispatcher() {
        this.producer = new KafkaProducer<>(properties());
    }

    private static Properties properties() {
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //propriedade para retornar somente os tópicos que já foram replicados do servidor
        //para ter certeza que o request foi completado e que o leader fez a replicação da msg
        //Por fim, temos a configuração de acks igual a all, que significa que o líder irá
        // esperar todas as réplicas que estão em sync (sincronizadas) receberem a informação.
        // Com todas em sincronia, podemos confirmar que a mensagem foi enviada,
        // porque se o líder cair, as réplicas têm essa informação.
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        return properties;
    }

    public void send(String topic, String key, CorrelationId id, T payload) throws ExecutionException, InterruptedException {
        Future<RecordMetadata> future = sendAsync(topic, key, id, payload);
        future.get();
    }

    public Future<RecordMetadata> sendAsync(String topic, String key, CorrelationId id, T payload) {
        //envia o valor com o correlation_id para identificaçao
        var value = new Message<>(id.continueWith("_" + topic), payload);
        var record = new ProducerRecord<>(topic, key, value);
        Callback callback = (data, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            System.out.println("sucesso enviando " + data.topic() + ":::partition " + data.partition() + "/ offset " + data.offset() + "/ timestamp " + data.timestamp());
        };
        return producer.send(record, callback);
    }


    //Interface que ajuda a fazer o fechamento tanto por exception quanto ap�s a finaliza��o do recurso
    @Override
    public void close() {
        producer.close();
    }
}
