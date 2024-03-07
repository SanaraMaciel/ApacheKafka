package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.dispatcher.KafkaDispatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GenerateAllReportServlet extends HttpServlet {

    private final KafkaDispatcher<String> batchDispatcher = new KafkaDispatcher<>();

    @Override
    public void destroy() {
        super.destroy();
        batchDispatcher.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            //dispatcher pra ficar escutando o tópico de relatórios
            //fazendo a tarefa em batch
            batchDispatcher.send("ECOMMERCE_SEND_MESSAGE_TO_ALL_USERS",
                    "ECOMMERCE_USER_GENERATE_READING_REPORT",
                    new CorrelationId(GenerateAllReportServlet.class.getSimpleName()),
                    "ECOMMERCE_USER_GENERATE_READING_REPORT");

            System.out.println("Enviando relatório a todos os usuários");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("Relatórios gerados e enviados!");

        } catch (ExecutionException e) {
            throw new ServletException(e);
        } catch (InterruptedException e) {
            throw new ServletException(e);
        }
    }

}
