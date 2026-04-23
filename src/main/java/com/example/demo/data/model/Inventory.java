package com.example.demo.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String productId;
    private Integer availableQty;
    private Integer reservedQty;
    private Integer soldQty;
    private Integer lowStockThreshold;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}