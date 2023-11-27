package br.com.sanara.ecommerce;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //criando um produtor em kafka
        var producer = new KafkaProducer<String, String>(properties());

        //variavel usada para ser a chave e o valor
        var key = "pedido2";
        var value = "123123";

        //registro do producer que vai armazenar o que vc envia pro kafka
        var record = new ProducerRecord("ECOMMERCE_NEW_ORDER", key, value);

        //enviando um record pelo producer send é assíncrono o get faz com que vc espere a finalização
        //passar um callback pro kafka pra poder ver se deu certo ou não
        //como se tivesse fazendo um observer para ver o resultado
        producer.send(record, (data, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return;
            }
            System.out.println("sucesso enviando: " + data.topic() + ":::partition: " + data.partition() + "/ offset "
                    + data.offset() + "/ timestamp" + data.timestamp());
        }).get();

    }

    private static Properties properties() {
        var properties = new Properties();

        //mostra aonde vai ser a conexão com o kafka
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        //informa os transformadores de string para bytes
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //informa os transformador para a msg de volta de string em bytes
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return properties;
    }


}
