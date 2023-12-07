package br.com.sanara.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMainOld {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //criando um produtor em kafka
        var producer = new KafkaProducer<String, String>(properties());

        for (var i = 0; i < 10; i++) {
//variavel usada para ser a chave e o valor
            var key = UUID.randomUUID().toString();
            var value = key + "30000,8000";

            //registro do producer que vai armazenar o que vc envia pro kafka
            var record = new ProducerRecord("ECOMMERCE_NEW_ORDER", key, value);

            //enviando um record pelo producer send é assíncrono o get faz com que vc espere a finalização
            //passar um callback pro kafka pra poder ver se deu certo ou não
            //como se tivesse fazendo um observer para ver o resultado
            Callback callback = (data, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                System.out.println("sucesso enviando: " + data.topic() + ":::partition: " + data.partition() + "/ offset "
                        + data.offset() + "/ timestamp" + data.timestamp());
            };
            producer.send(record, callback).get();

            //enviando um novo record no tópico
            var keyEmail = UUID.randomUUID().toString();

            var email = "Bem Vindo! estamos processando sua compra";
            var emailRecord = new ProducerRecord<>("ECOMMERCE_SEND_EMAIL", keyEmail, email);
            producer.send(emailRecord).get();
        }

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
