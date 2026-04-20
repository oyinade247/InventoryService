package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull(message = "Available quantity cannot be null")
    @Min(value = 0, message = "Available quantity cannot be less than 0")
    private Integer availableQty;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Integer availableQty) {
        this.availableQty = availableQty;
    }

    public Integer getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(Integer availableQty) {
        this.availableQty = availableQty;
    }
}

