package br.com.sanara.ecommerce;

public interface ServiceFactory<T> {
    ConsumerService<T> create();
}
