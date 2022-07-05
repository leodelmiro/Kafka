package br.com.leodelmiro.ecommerce;

import br.com.leodelmiro.ecommerce.consumer.KafkaService;
import br.com.leodelmiro.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EmailNewOrderService {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var fraudService = new EmailNewOrderService();
        try (var service = new KafkaService(EmailNewOrderService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                fraudService::parse,
                Map.of())
        ) {
            service.run();
        }
    }

    private final KafkaDispatcher<Email> emailDispatcher = new KafkaDispatcher<>();

    private void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
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
