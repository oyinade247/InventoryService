package com.example.demo.controllers;

import com.example.demo.dto.LowStockResponse;
import com.example.demo.dto.StockUpdateRequest;
import com.example.demo.data.model.Inventory;
import com.example.demo.data.repositories.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable String id) {
        Optional<Inventory> inventory = inventoryRepository.findById(Integer.parseInt(id));
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        if (inventory.getAvailableQty() == null) {
            inventory.setAvailableQty(0);
        }
        if (inventory.getReservedQty() == null) {
            inventory.setReservedQty(0);
        }
        if (inventory.getSoldQty() == null) {
            inventory.setSoldQty(0);
        }
        Inventory savedInventory = inventoryRepository.save(inventory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable String id, @RequestBody Inventory inventoryDetails) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(Integer.parseInt(id));
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            if (inventoryDetails.getProductId() != null) {
                inventory.setProductId(inventoryDetails.getProductId());
            }
            if (inventoryDetails.getAvailableQty() != null) {
                inventory.setAvailableQty(inventoryDetails.getAvailableQty());
            }
            if (inventoryDetails.getReservedQty() != null) {
                inventory.setReservedQty(inventoryDetails.getReservedQty());
            }
            if (inventoryDetails.getSoldQty() != null) {
                inventory.setSoldQty(inventoryDetails.getSoldQty());
            }
            if (inventoryDetails.getLowStockThreshold() != null) {
                inventory.setLowStockThreshold(inventoryDetails.getLowStockThreshold());
            }
            Inventory updatedInventory = inventoryRepository.save(inventory);
            return ResponseEntity.ok(updatedInventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String id) {
        Optional<Inventory> inventory = inventoryRepository.findById(Integer.parseInt(id));
        if (inventory.isPresent()) {
            inventoryRepository.deleteById(Integer.parseInt(id));
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/adjust-stock")
    public ResponseEntity<Inventory> adjustStock(@PathVariable String id, @RequestParam Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(Integer.parseInt(id));
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            int currentQty = inventory.getAvailableQty() != null ? inventory.getAvailableQty() : 0;
            inventory.setAvailableQty(currentQty + quantity);
            Inventory updatedInventory = inventoryRepository.save(inventory);
            return ResponseEntity.ok(updatedInventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getInventoryByProductId(@PathVariable String productId) {
        List<Inventory> inventories = inventoryRepository.findAll()
                .stream()
                .filter(inv -> inv.getProductId() != null && inv.getProductId().equals(productId))
                .toList();
        return ResponseEntity.ok(inventories);
    }


    @PutMapping("/{productId}/stock")
    public ResponseEntity<?> updateAbsoluteStock(@PathVariable String productId, @Valid @RequestBody StockUpdateRequest request) {
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductId(productId);
        
        if (optionalInventory.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Product not found: " + productId));
        }

        Inventory inventory = optionalInventory.get();
        
        if (request.getAvailableQty() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Available quantity cannot be less than 0"));
        }

        inventory.setAvailableQty(request.getAvailableQty());
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        return ResponseEntity.ok(updatedInventory);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockResponse>> getLowStockInventory() {
        List<Inventory> lowStockItems = inventoryRepository.findByAvailableQtyLessThanLowStockThreshold();
        
        List<LowStockResponse> response = lowStockItems.stream()
                .map(item -> new LowStockResponse(item.getProductId(), item.getAvailableQty()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
