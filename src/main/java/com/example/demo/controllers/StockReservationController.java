package com.example.demo.controllers;

import com.example.demo.data.model.StockReservation;
import com.example.demo.data.model.StockStatus;
import com.example.demo.data.repositories.StockReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock-reservations")
public class StockReservationController {

    @Autowired
    private StockReservationRepository stockReservationRepository;

    @GetMapping
    public ResponseEntity<List<StockReservation>> getAllReservations() {
        List<StockReservation> reservations = stockReservationRepository.findAll();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockReservation> getReservationById(@PathVariable String id) {
        Optional<StockReservation> reservation = stockReservationRepository.findById(id);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<StockReservation> createReservation(@RequestBody StockReservation reservation) {
        if (reservation.getStatus() == null) {
            reservation.setStatus(StockStatus.RESERVED);
        }
        StockReservation savedReservation = stockReservationRepository.save(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockReservation> updateReservation(@PathVariable String id, @RequestBody StockReservation reservationDetails) {
        Optional<StockReservation> optionalReservation = stockReservationRepository.findById(id);
        if (optionalReservation.isPresent()) {
            StockReservation reservation = optionalReservation.get();
            if (reservationDetails.getInventoryId() != null) {
                reservation.setInventoryId(reservationDetails.getInventoryId());
            }
            if (reservationDetails.getOrderId() != null) {
                reservation.setOrderId(reservationDetails.getOrderId());
            }
            if (reservationDetails.getQuantity() != null) {
                reservation.setQuantity(reservationDetails.getQuantity());
            }
            if (reservationDetails.getStatus() != null) {
                reservation.setStatus(reservationDetails.getStatus());
            }
            if (reservationDetails.getExpiresAt() != null) {
                reservation.setExpiresAt(reservationDetails.getExpiresAt());
            }
            StockReservation updatedReservation = stockReservationRepository.save(reservation);
            return ResponseEntity.ok(updatedReservation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable String id) {
        Optional<StockReservation> reservation = stockReservationRepository.findById(id);
        if (reservation.isPresent()) {
            stockReservationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<StockReservation> confirmReservation(@PathVariable String id) {
        Optional<StockReservation> optionalReservation = stockReservationRepository.findById(id);
        if (optionalReservation.isPresent()) {
            StockReservation reservation = optionalReservation.get();
            reservation.setStatus(StockStatus.CONFIRMED);
            StockReservation updatedReservation = stockReservationRepository.save(reservation);
            return ResponseEntity.ok(updatedReservation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<StockReservation> releaseReservation(@PathVariable String id) {
        Optional<StockReservation> optionalReservation = stockReservationRepository.findById(id);
        if (optionalReservation.isPresent()) {
            StockReservation reservation = optionalReservation.get();
            reservation.setStatus(StockStatus.RELEASED);
            StockReservation updatedReservation = stockReservationRepository.save(reservation);
            return ResponseEntity.ok(updatedReservation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<StockReservation>> getReservationsByInventoryId(@PathVariable String inventoryId) {
        List<StockReservation> reservations = stockReservationRepository.findAll()
                .stream()
                .filter(res -> res.getInventoryId() != null && res.getInventoryId().equals(inventoryId))
                .toList();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<StockReservation>> getReservationsByOrderId(@PathVariable String orderId) {
        List<StockReservation> reservations = stockReservationRepository.findAll()
                .stream()
                .filter(res -> res.getOrderId() != null && res.getOrderId().equals(orderId))
                .toList();
        return ResponseEntity.ok(reservations);
    }
}
