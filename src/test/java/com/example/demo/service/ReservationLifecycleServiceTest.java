package com.example.demo.service;

import com.example.demo.data.model.Inventory;
import com.example.demo.data.model.StockReservation;
import com.example.demo.data.model.StockStatus;
import com.example.demo.data.repositories.InventoryRepository;
import com.example.demo.data.repositories.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationLifecycleServiceTest {

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    private ReservationLifecycleService service;

    @BeforeEach
    void setUp() {
        service = new ReservationLifecycleService(reservationRepository, inventoryRepository);
    }

    @Test
    void testConfirmReservation_IN003() {
        // Given: reservation for qty=3
        String reservationId = "res-123";
        String inventoryId = "inv-456";

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setAvailableQty(10);
        inventory.setReservedQty(3);
        inventory.setSoldQty(5);

        StockReservation reservation = new StockReservation();
        reservation.setId(reservationId);
        reservation.setInventoryId(inventoryId);
        reservation.setQuantity(3);
        reservation.setStatus(StockStatus.RESERVED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.save(any(StockReservation.class))).thenReturn(reservation);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When: confirm
        StockReservation result = service.confirmReservation(reservationId);

        // Then: available unchanged, reserved-=3, sold+=3
        assertEquals(StockStatus.CONFIRMED, result.getStatus());
        assertEquals(10, inventory.getAvailableQty()); // unchanged
        assertEquals(0, inventory.getReservedQty()); // 3 - 3 = 0
        assertEquals(8, inventory.getSoldQty()); // 5 + 3 = 8

        verify(reservationRepository).save(reservation);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void testReleaseReservation_IN004() {
        // Given: reservation for qty=3
        String reservationId = "res-123";
        String inventoryId = "inv-456";

        Inventory inventory = new Inventory();
        inventory.setId(inventoryId);
        inventory.setAvailableQty(7);
        inventory.setReservedQty(3);
        inventory.setSoldQty(5);

        StockReservation reservation = new StockReservation();
        reservation.setId(reservationId);
        reservation.setInventoryId(inventoryId);
        reservation.setQuantity(3);
        reservation.setStatus(StockStatus.RESERVED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(reservationRepository.save(any(StockReservation.class))).thenReturn(reservation);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When: release
        StockReservation result = service.releaseReservation(reservationId);

        // Then: available+=3, reserved-=3
        assertEquals(StockStatus.RELEASED, result.getStatus());
        assertEquals(10, inventory.getAvailableQty());
        assertEquals(0, inventory.getReservedQty());
        assertEquals(5, inventory.getSoldQty());

        verify(reservationRepository).save(reservation);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void testConfirmReservation_NotFound() {
        String reservationId = "non-existent";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            service.confirmReservation(reservationId);
        });
    }

    @Test
    void testConfirmReservation_NotReservedStatus() {
        String reservationId = "res-123";
        StockReservation reservation = new StockReservation();
        reservation.setId(reservationId);
        reservation.setStatus(StockStatus.CONFIRMED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> {
            service.confirmReservation(reservationId);
        });
    }

    @Test
    void testReleaseReservation_NotFound() {
        String reservationId = "non-existent";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            service.releaseReservation(reservationId);
        });
    }

    @Test
    void testReleaseReservation_NotReservedStatus() {
        String reservationId = "res-123";
        StockReservation reservation = new StockReservation();
        reservation.setId(reservationId);
        reservation.setStatus(StockStatus.RELEASED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> {
            service.releaseReservation(reservationId);
        });
    }
}