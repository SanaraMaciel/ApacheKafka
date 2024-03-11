package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderServlet extends HttpServlet {

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        orderDispatcher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {

            //lendo os valores que são passados por requisição
            var email = req.getParameter("email");
            var amount = new BigDecimal(req.getParameter("amount"));

            //recendo o uuid através da requisição
            var orderId = req.getParameter("uuid");
            //var orderId = UUID.randomUUID().toString();
            var order = new Order(orderId, amount, email);

            //abrir a conexão com o banco de dados para verificar se a order já foi enviada
            //com isso só dispara o dispatcher se não foi enviada
            try (var database = new OrdersDatabase()) {
                if (database.saveNew(order)) {
                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, new CorrelationId(NewOrderServlet.class.getSimpleName()), order);
                    //mostrando a resposta na tela do browser com o código 200
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("Processo da nova compra finalizado.");
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().println("Ordem já recebida.");
                }
            }
        } catch (ExecutionException | InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
