package br.com.sanara.ecommerce;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
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
            batchDispatcher.send("SEND_MESSAGE_TO_ALL_USERS", "USER_GENERATE_READING_REPORT",
                    "TESTE");

            System.out.println("Mensagem enviada cm o relatorio de todos os usuarios.");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("Relatorios gerados.");


        } catch (ExecutionException e) {
            throw new ServletException(e);
        } catch (InterruptedException e) {
            throw new ServletException(e);
        }

    }
}
