package br.com.sanara.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

class KafkaDispatcher<T> implements Closeable {

    private final KafkaProducer<String, T> producer;

    KafkaDispatcher() {
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

    void send(String topic, String key, T value) throws ExecutionException, InterruptedException {
        var record = new ProducerRecord<>(topic, key, value);
        Callback callback = (data, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            System.out.println("sucesso enviando " + data.topic() + ":::partition " + data.partition() + "/ offset " + data.offset() + "/ timestamp " + data.timestamp());
        };
        producer.send(record, callback).get();
    }

    //Interface que ajuda a fazer o fechamento tanto por exception quanto ap�s a finaliza��o do recurso
    @Override
    public void close() {
        producer.close();
    }
}
