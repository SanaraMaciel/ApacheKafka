package br.com.sanara.ecommerce;

import br.com.sanara.ecommerce.consumer.ConsumerService;
import br.com.sanara.ecommerce.consumer.ServiceRunner;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

/**
 * Servico para geracao de relatórios em batch
 * refatorado para rodar com threads em paralelo
 */
public class ReadingReportService implements ConsumerService<User> {

    private static final Path SOURCE = new File("src/main/resources/report.txt").toPath();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ServiceRunner(ReadingReportService::new).start(5);
    }

    @Override
    public void parse(ConsumerRecord<String, Message<User>> record) throws IOException {
        System.out.println("------------------------------------------");
        System.out.println("Processando relatório para " + record.value());

        var message = record.value();
        var user = message.getPayload();
        var target = new File(user.getReportPath());
        IO.copyTo(SOURCE, target);
        IO.append(target, "Criado por " + user.getUuid());

        System.out.println("Arquivo criado: " + target.getAbsolutePath());
    }
    @Override
    public String getTopic() {
        return "ECOMMERCE_USER_GENERATE_READING_REPORT";
    }
    @Override
    public String getConsumerGroup() {
        return ReadingReportService.class.getSimpleName();
    }
}
