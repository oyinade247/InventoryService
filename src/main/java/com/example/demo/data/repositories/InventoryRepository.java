package com.example.demo.data.repositories;

import com.example.demo.data.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer>{

    @Query("SELECT i FROM Inventory i WHERE i.availableQty < i.lowStockThreshold")
    List<Inventory> findByAvailableQtyLessThanLowStockThreshold();

    Optional<Inventory> findByProductId(String productId);
}
