package br.com.sanara.ecommerce;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * classe de teste refatorada
 */
public class NewOrderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //método para gerar o producer do kafka
        try ( var dispatcher = new KafkaDispatcher()) {
            for (var i = 0; i < 10; i++) {

                //variavel usada para ser a chave e o valor
                var key = UUID.randomUUID().toString();
                var value = key + "70000,9000";
                dispatcher.send("ECOMMERCE_NEW_ORDER", key, value);

                //enviando um novo record no tópico
                var keyEmail = UUID.randomUUID().toString();
                var email = "Bem Vindo! estamos processando sua compra";
                dispatcher.send("ECOMMERCE_SEND_EMAIL", keyEmail, email);
            }

        }
    }
}
