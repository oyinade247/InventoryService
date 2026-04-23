package com.example.demo.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class StockReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String inventoryId;
    private String orderId;
    private Integer quantity;
    private StockStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}