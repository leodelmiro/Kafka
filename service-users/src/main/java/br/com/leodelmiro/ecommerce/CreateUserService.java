package br.com.leodelmiro.ecommerce;

import br.com.leodelmiro.ecommerce.consumer.ConsumerService;
import br.com.leodelmiro.ecommerce.consumer.ServiceRunner;
import br.com.leodelmiro.ecommerce.database.LocalDatabase;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.SQLException;
import java.util.UUID;

public class CreateUserService implements ConsumerService<Order> {

    private final LocalDatabase database;

    CreateUserService() throws SQLException {
        this.database = new LocalDatabase("users_database");
        this.database.createIfNotExists("create table Users (" +
                "uuid varchar(200) primary key," +
                "email varchar(200))");
    }

    public static void main(String[] args) {
        new ServiceRunner<>(CreateUserService::new).start(1);
    }

    @Override
    public String getTopic() {
        return "ECOMMERCE_NEW_ORDER";
    }

    @Override
    public String getConsumerGroup() {
        return CreateUserService.class.getSimpleName();
    }

    public void parse(ConsumerRecord<String, Message<Order>> record) throws SQLException {
        var message = record.value();
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking new user");
        System.out.println(message.getPayload());
        var order = message.getPayload();
        if (isNewUser(order.getEmail())) {
            insertNewUser(order.getEmail());
        }
    }


    private void insertNewUser(String email) throws SQLException {
        String uuid = UUID.randomUUID().toString();
        database.update("insert into Users (uuid, email) values (?,?)", uuid, email);
        System.out.println("Usuário " + uuid + "e email " + email + "adicionado");
    }

    private boolean isNewUser(String email) throws SQLException {
        var results = database.query("select uuid from Users where email = ? limit 1", email);
        return !results.next();
    }
}
