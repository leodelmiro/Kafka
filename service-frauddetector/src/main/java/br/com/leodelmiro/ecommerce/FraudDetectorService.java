package br.com.leodelmiro.ecommerce;

import br.com.leodelmiro.ecommerce.consumer.ConsumerService;
import br.com.leodelmiro.ecommerce.consumer.ServiceRunner;
import br.com.leodelmiro.ecommerce.database.LocalDatabase;
import br.com.leodelmiro.ecommerce.dispatcher.KafkaDispatcher;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class FraudDetectorService implements ConsumerService<Order> {

    private final LocalDatabase database;

    FraudDetectorService() throws SQLException {
        this.database = new LocalDatabase("frauds_database");
        this.database.createIfNotExists("create table Orders (" +
                "uuid varchar(200) primary key," +
                "is_fraud boolean)");
    }

    public static void main(String[] args) {
        new ServiceRunner<>(FraudDetectorService::new).start(1);
    }

    private final KafkaDispatcher<Order> orderKafkaDispatcher = new KafkaDispatcher<>();

    @Override
    public String getTopic() {
        return "ECOMMERCE_ORDER_REJECTED";
    }

    @Override
    public String getConsumerGroup() {
        return FraudDetectorService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException, SQLException {
        var message = record.value();
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking for fraud");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //ignoring
            e.printStackTrace();
        }
        var order = message.getPayload();
        if (wasProcessed(order)) {
            System.out.println("Order: " + order + " was already processed");
            return;
        }
        if (isFraud(order)) {
            database.update("insert into Orders (uuid, is_fraud) values (?, true)", order.getOrderId());
            //pretending that the fraud happens when amount is >= 4500
            System.out.println("Order is a fraud!!!!!");
            orderKafkaDispatcher.send("ECOMMERCE_ORDER_REJECTED",
                    order.getEmail(),
                    order,
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()));
        } else {
            database.update("insert into Orders (uuid, is_fraud) values (?, false)", order.getOrderId());
            System.out.println("Aproved: " + order);
            orderKafkaDispatcher.send("ECOMMERCE_ORDER_APPROVED",
                    order.getEmail(),
                    order,
                    message.getId().continueWith(FraudDetectorService.class.getSimpleName()));
        }
    }

    private boolean wasProcessed(Order order) throws SQLException {
        var results = database.query("select uuid from Orders where uuid = ? limit 1",
                order.getOrderId());
        return results.next();
    }


    private boolean isFraud(Order order) {
        return order.getAmount().compareTo(new BigDecimal("4500")) >= 0;
    }
}
