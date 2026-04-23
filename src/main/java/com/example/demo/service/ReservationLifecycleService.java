package com.example.demo.service;

import com.example.demo.data.model.Inventory;
import com.example.demo.data.model.StockReservation;
import com.example.demo.data.model.StockStatus;
import com.example.demo.data.repositories.InventoryRepository;
import com.example.demo.data.repositories.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReservationLifecycleService {

    private final StockReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;

    public ReservationLifecycleService(StockReservationRepository reservationRepository,
            InventoryRepository inventoryRepository) {
        this.reservationRepository = reservationRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public StockReservation confirmReservation(String reservationId) {
        Optional<StockReservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }

        StockReservation reservation = reservationOpt.get();
        if (reservation.getStatus() != StockStatus.RESERVED) {
            throw new IllegalStateException("Reservation is not in RESERVED status");
        }

        // Change status to CONFIRMED
        reservation.setStatus(StockStatus.CONFIRMED);
        reservationRepository.save(reservation);

        // Fetch parent Inventory record
        Inventory inventory = inventoryRepository.findById(reservation.getInventoryId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Inventory not found: " + reservation.getInventoryId()));

        // Decrement reserved_qty by reservation qty
        inventory.setReservedQty(inventory.getReservedQty() - reservation.getQuantity());
        // Increment sold_qty by reservation qty
        inventory.setSoldQty(inventory.getSoldQty() + reservation.getQuantity());
        // available_qty is unchanged (already decremented during reservation)
        inventoryRepository.save(inventory);

        return reservation;
    }

    @Transactional
    public StockReservation releaseReservation(String reservationId) {
        Optional<StockReservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isEmpty()) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }

        StockReservation reservation = reservationOpt.get();
        if (reservation.getStatus() != StockStatus.RESERVED) {
            throw new IllegalStateException("Reservation is not in RESERVED status");
        }

        // Change status to RELEASED
        reservation.setStatus(StockStatus.RELEASED);
        reservationRepository.save(reservation);

        // Fetch parent Inventory record
        Inventory inventory = inventoryRepository.findById(reservation.getInventoryId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Inventory not found: " + reservation.getInventoryId()));

        // Decrement reserved_qty by qty
        inventory.setReservedQty(inventory.getReservedQty() - reservation.getQuantity());
        // Increment available_qty by qty (restoring the stock)
        inventory.setAvailableQty(inventory.getAvailableQty() + reservation.getQuantity());
        inventoryRepository.save(inventory);

        return reservation;
    }
}