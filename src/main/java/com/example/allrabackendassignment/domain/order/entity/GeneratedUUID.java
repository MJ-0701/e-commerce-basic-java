package com.example.allrabackendassignment.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GeneratedUUID implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "order_id")
    private String orderId = "";

    public GeneratedUUID() {
    }

    public GeneratedUUID(String orderId) {
        this.orderId = orderId;
    }

    public static String generate(String prefix, LocalDateTime time) {
        if (time == null) {
            time = LocalDateTime.now();
        }
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.getDefault());
        return prefix + time.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + uuid;
    }

    public static GeneratedUUID create(String prefix, LocalDateTime time) {
        if (time == null) {
            time = LocalDateTime.now();
        }
        return new GeneratedUUID(generate(prefix, time));
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneratedUUID)) return false;
        GeneratedUUID that = (GeneratedUUID) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return orderId;
    }
}
