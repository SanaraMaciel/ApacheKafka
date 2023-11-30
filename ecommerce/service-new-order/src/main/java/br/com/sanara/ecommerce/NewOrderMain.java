package br.com.sanara.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * classe de teste refatorada
 */
public class NewOrderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //método para gerar o producer do kafka
        try (var orderDispatcher = new KafkaDispatcher<Order>()) {
            try (var emailDispatcher = new KafkaDispatcher<Email>()) {
                for (var i = 0; i < 10; i++) {

                    //variavel usada para ser a chave e o valor
                    var userId = UUID.randomUUID().toString();
                    var orderId = UUID.randomUUID().toString();
                    var amount = new BigDecimal(Math.random() * 5000 + 1);
                    var order = new Order(userId, orderId, amount);

                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", userId, order);

                    //enviando um novo record no tópico
                    var keyEmail = UUID.randomUUID().toString();
                    Email email = new Email("teste","Bem");
                    //String email = "teste meu Bem";
                    emailDispatcher.send("ECOMMERCE_SEND_EMAIL", keyEmail, email);
                }

            }
        }
    }
}
