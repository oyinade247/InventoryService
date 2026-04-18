package com.example.demo.data.repositories;

import com.example.demo.data.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Integer>{
}
