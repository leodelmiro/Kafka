package br.com.leodelmiro.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerService<T> {
    void parse(ConsumerRecord<String, Message<T>> record);
    String getTopic();
    String getConsumerGroup();
}
