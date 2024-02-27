package br.com.sanara.ecommerce;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class IO {


    public static void cotyTo(Path source, File target) throws IOException {
        //copia o arquivo do diretório pai e substitui caso exista
        target.getParentFile().mkdirs();
        Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);

    }

    public static void append(File target, String content) throws IOException {
        //adiciona no arquivo a linha do cnteudo
        Files.write(target.toPath(), content.getBytes(), StandardOpenOption.APPEND);
    }
}
