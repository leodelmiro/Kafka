package br.com.leodelmiro.ecommerce;

public interface ServiceFactory<T> {
    ConsumerService<T> create();
}
