package com.example.demo.controllers;

import com.example.demo.dto.LowStockResponse;
import com.example.demo.dto.StockUpdateRequest;
import com.example.demo.data.model.Inventory;
import com.example.demo.data.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryControllerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryController inventoryController;

    private Inventory testInventory;
    private StockUpdateRequest stockUpdateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testInventory = new Inventory();
        testInventory.setId("1");
        testInventory.setProductId("PROD-001");
        testInventory.setAvailableQty(100);
        testInventory.setReservedQty(20);
        testInventory.setSoldQty(30);
        testInventory.setLowStockThreshold(25);
        testInventory.setCreatedAt(LocalDateTime.now());
        testInventory.setUpdatedAt(LocalDateTime.now());

        stockUpdateRequest = new StockUpdateRequest(50);
    }


    @Test
    void testUpdateAbsoluteStock_Success() {
        when(inventoryRepository.findByProductId("PROD-001")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        ResponseEntity<?> response = inventoryController.updateAbsoluteStock("PROD-001", stockUpdateRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Inventory updatedInventory = (Inventory) response.getBody();
        assertNotNull(updatedInventory);
        assertEquals(50, updatedInventory.getAvailableQty());
        assertEquals(20, updatedInventory.getReservedQty());
        assertEquals(30, updatedInventory.getSoldQty());

        verify(inventoryRepository, times(1)).findByProductId("PROD-001");
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testUpdateAbsoluteStock_ProductNotFound() {
        when(inventoryRepository.findByProductId("NONEXISTENT")).thenReturn(Optional.empty());

        ResponseEntity<?> response = inventoryController.updateAbsoluteStock("NONEXISTENT", stockUpdateRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof InventoryController.ErrorResponse);

        verify(inventoryRepository, times(1)).findByProductId("NONEXISTENT");
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testUpdateAbsoluteStock_NegativeQuantity() {
        StockUpdateRequest negativeRequest = new StockUpdateRequest(-10);
        when(inventoryRepository.findByProductId("PROD-001")).thenReturn(Optional.of(testInventory));

        ResponseEntity<?> response = inventoryController.updateAbsoluteStock("PROD-001", negativeRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof InventoryController.ErrorResponse);

        verify(inventoryRepository, times(1)).findByProductId("PROD-001");
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testUpdateAbsoluteStock_ZeroQuantity() {
        StockUpdateRequest zeroRequest = new StockUpdateRequest(0);
        when(inventoryRepository.findByProductId("PROD-001")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        ResponseEntity<?> response = inventoryController.updateAbsoluteStock("PROD-001", zeroRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Inventory updatedInventory = (Inventory) response.getBody();
        assertNotNull(updatedInventory);
        assertEquals(0, updatedInventory.getAvailableQty());

        verify(inventoryRepository, times(1)).findByProductId("PROD-001");
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }


    @Test
    void testGetLowStockInventory_Success() {
        Inventory lowStock1 = new Inventory();
        lowStock1.setProductId("PROD-002");
        lowStock1.setAvailableQty(10);
        lowStock1.setLowStockThreshold(25);

        Inventory lowStock2 = new Inventory();
        lowStock2.setProductId("PROD-003");
        lowStock2.setAvailableQty(5);
        lowStock2.setLowStockThreshold(20);

        List<Inventory> lowStockItems = Arrays.asList(lowStock1, lowStock2);
        when(inventoryRepository.findByAvailableQtyLessThanLowStockThreshold()).thenReturn(lowStockItems);

        ResponseEntity<List<LowStockResponse>> response = inventoryController.getLowStockInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        LowStockResponse first = response.getBody().get(0);
        assertEquals("PROD-002", first.getProductId());
        assertEquals(10, first.getAvailableQty());

        LowStockResponse second = response.getBody().get(1);
        assertEquals("PROD-003", second.getProductId());
        assertEquals(5, second.getAvailableQty());

        verify(inventoryRepository, times(1)).findByAvailableQtyLessThanLowStockThreshold();
    }

    @Test
    void testGetLowStockInventory_Empty() {
        when(inventoryRepository.findByAvailableQtyLessThanLowStockThreshold()).thenReturn(Arrays.asList());

        ResponseEntity<List<LowStockResponse>> response = inventoryController.getLowStockInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(inventoryRepository, times(1)).findByAvailableQtyLessThanLowStockThreshold();
    }

    @Test
    void testGetLowStockInventory_MultipleItems() {
        Inventory lowStock1 = new Inventory();
        lowStock1.setProductId("PROD-A");
        lowStock1.setAvailableQty(5);
        lowStock1.setLowStockThreshold(25);

        Inventory lowStock2 = new Inventory();
        lowStock2.setProductId("PROD-B");
        lowStock2.setAvailableQty(2);
        lowStock2.setLowStockThreshold(10);

        Inventory lowStock3 = new Inventory();
        lowStock3.setProductId("PROD-C");
        lowStock3.setAvailableQty(15);
        lowStock3.setLowStockThreshold(20);

        List<Inventory> lowStockItems = Arrays.asList(lowStock1, lowStock2, lowStock3);
        when(inventoryRepository.findByAvailableQtyLessThanLowStockThreshold()).thenReturn(lowStockItems);

        ResponseEntity<List<LowStockResponse>> response = inventoryController.getLowStockInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("PROD-A", response.getBody().get(0).getProductId());
        assertEquals("PROD-B", response.getBody().get(1).getProductId());
        assertEquals("PROD-C", response.getBody().get(2).getProductId());
    }


    @Test
    void testGetAllInventory() {
        List<Inventory> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findAll()).thenReturn(inventories);

        ResponseEntity<List<Inventory>> response = inventoryController.getAllInventory();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    void testGetInventoryById_Found() {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(testInventory));

        ResponseEntity<Inventory> response = inventoryController.getInventoryById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PROD-001", response.getBody().getProductId());
    }

    @Test
    void testGetInventoryById_NotFound() {
        when(inventoryRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<Inventory> response = inventoryController.getInventoryById("999");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

