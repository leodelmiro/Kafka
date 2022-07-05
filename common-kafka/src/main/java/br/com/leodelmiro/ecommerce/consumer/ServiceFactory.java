package br.com.leodelmiro.ecommerce.consumer;

public interface ServiceFactory<T> {
    ConsumerService<T> create();
}
