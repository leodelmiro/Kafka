package br.com.leodelmiro.ecommerce;

import br.com.leodelmiro.ecommerce.consumer.ConsumerService;
import br.com.leodelmiro.ecommerce.consumer.ServiceRunner;
import br.com.leodelmiro.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.ExecutionException;

public class EmailNewOrderService implements ConsumerService<Order> {

    public static void main(String[] args) {
        new ServiceRunner<>(EmailNewOrderService::new).start(1);
    }

    private final KafkaDispatcher<Email> emailDispatcher = new KafkaDispatcher<>();

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return EmailNewOrderService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        var message = record.value();
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, preparing email");
        System.out.println(record.key());
        System.out.println(message);
        System.out.println(record.partition());
        System.out.println(record.offset());

        var subject = "Order";
        var body = "Thank You for your order! We are processing your order!";
        var emailMessage = new Email(subject, body);
        var order = message.getPayload();
        emailDispatcher.send("ECOMMERCE_SEND_EMAIL",
                order.getEmail(),
                emailMessage,
                message.getId().continueWith(EmailNewOrderService.class.getSimpleName()
                )
        );
    }
}
