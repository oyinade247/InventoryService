package com.example.demo.dto;

public class LowStockResponse {
    private String productId;
    private Integer availableQty;

    public LowStockResponse(String productId, Integer availableQty) {
        this.productId = productId;
        this.availableQty = availableQty;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }
}
