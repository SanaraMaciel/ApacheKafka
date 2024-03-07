package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;

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
                var email = Math.random() + "@email.com";
                for (var i = 0; i < 10; i++) {

                    var id = new CorrelationId(NewOrderMain.class.getSimpleName());

                    var orderId = UUID.randomUUID().toString();
                    var amount = new BigDecimal(Math.random() * 5000 + 1);

                    var order = new Order(orderId, amount, email);
                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, id, order);

                    Email emailCode = new Email("email","obrigado por comprar com a gente");
                    emailDispatcher.send("ECOMMERCE_SEND_EMAIL", email, id, emailCode);
                }

            }
        }
    }
}
