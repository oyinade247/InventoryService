package com.example.demo.data.repositories;

import com.example.demo.data.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, String> {
}
