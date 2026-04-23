package com.example.demo.controller;

import com.example.demo.data.model.StockReservation;
import com.example.demo.service.ReservationLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final ReservationLifecycleService reservationLifecycleService;

    public InventoryController(ReservationLifecycleService reservationLifecycleService) {
        this.reservationLifecycleService = reservationLifecycleService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<StockReservation> confirmReservation(@RequestBody Map<String, String> request) {
        String reservationId = request.get("reservationId");
        String orderId = request.get("orderId");

        String id = reservationId != null ? reservationId : orderId;
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        StockReservation reservation = reservationLifecycleService.confirmReservation(id);
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/release")
    public ResponseEntity<StockReservation> releaseReservation(@RequestBody Map<String, String> request) {
        String reservationId = request.get("reservationId");
        String orderId = request.get("orderId");

        String id = reservationId != null ? reservationId : orderId;
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        StockReservation reservation = reservationLifecycleService.releaseReservation(id);
        return ResponseEntity.ok(reservation);
    }
}