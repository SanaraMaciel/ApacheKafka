package br.com.sanara.ecommerce;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HttpEcommerceService {

    public static void main(String[] args) throws Exception {

        //cria um servidor com o jetty
        var server = new Server(8080);


        //cria o contexto com os caminhos passados
        var context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new NewOrderServlet()), "/new");
        server.setHandler(context);

        //faz o start do servidor
        server.start();

       //faz o servidor ficar aguardando até finalizar a requisição
        server.join();
    }

}
