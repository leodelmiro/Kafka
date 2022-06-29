package br.com.leodelmiro.ecommerce;

import java.util.UUID;

public class CorrelationId {
    private final String id;

    CorrelationId(String title) {
        id = title + "(" + UUID.randomUUID() + ")";
    }

    public CorrelationId continueWith(String title) {
        return new CorrelationId(id + "-" + title);
    }

    @Override
    public String toString() {
        return "CorrelationId{" +
                "id='" + id + '\'' +
                '}';
    }
}
