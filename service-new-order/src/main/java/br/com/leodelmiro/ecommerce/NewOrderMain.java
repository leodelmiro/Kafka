package br.com.leodelmiro.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (var orderDispatcher = new KafkaDispatcher<Order>()) {
            try (var emailDispatcher = new KafkaDispatcher<Email>()) {
                var email = Math.random() + "@email.com";
                for (var i = 0; i < 10; i++) {
                    var orderId = UUID.randomUUID().toString();
                    var amount = new BigDecimal(Math.random() * 5000 + 1);

                    var order = new Order(orderId, amount, email);
                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, order);

                    var subject = "Order";
                    var body = "Thank You for your order! We are processing your order!";
                    var emailMessage = new Email(subject, body);
                    emailDispatcher.send("ECOMMERCE_SEND_EMAIL", email, emailMessage);
                }
            }

        }
    }
}
