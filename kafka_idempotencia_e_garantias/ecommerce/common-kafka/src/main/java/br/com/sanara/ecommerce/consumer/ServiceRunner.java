package br.com.sanara.ecommerce.consumer;

import java.util.concurrent.Executors;

/**
 * Classe que cria um provider e o submete várias vezes conforme passado por parâmetro
 * @param <T>
 */
public class ServiceRunner<T> {

    private final ServiceProvider<T> provider;

    public ServiceRunner(ServiceFactory<T> factory) {
        this.provider = new ServiceProvider<>(factory);
    }

    public void start(int threadCount) {
        var pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i <= threadCount; i++) {
            pool.submit(provider);
        }

    }
}