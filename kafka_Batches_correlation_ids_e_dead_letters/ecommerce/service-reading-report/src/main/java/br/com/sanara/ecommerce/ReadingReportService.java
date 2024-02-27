package br.com.sanara.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Servico para geracao de relatórios em batch
 */
public class ReadingReportService {

    private static final Path SOURCE = new File("src/main/resources/report.txt").toPath();
    private final KafkaDispatcher<User> orderDispatcher = new KafkaDispatcher<>();

    public static void main(String[] args) {
        var reportService = new ReadingReportService();
        try (var service = new KafkaService(ReadingReportService.class.getSimpleName(),
                "USER_GENERATE_READING_REPORT", reportService::parse, User.class,
                Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, User> record) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("Processando novo relatório para o usuário " + record.value());

        //gerando o relatório txt através de um arquivo modelo
        var user = record.value();
        var target = new File(user.getReportPath());
        IO.cotyTo(SOURCE, target);
        IO.append(target, "Criado por: " + user.getUuid());

        System.out.println("Arquivo criado: " + target.getAbsolutePath());

        //depois disso voce pode enviar o email com o relatório para o usuario
    }

}
