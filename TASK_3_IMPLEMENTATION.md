# Task 3: Seller Stock Management APIs - Implementation Summary

## Overview
This implementation provides REST APIs for sellers and admins to manage absolute stock levels and generate reports on low-stock inventory items.

## Features Implemented

### 1. **PUT /api/inventory/{productId}/stock** (JWT Auth Ready)
- **Description**: Allows a seller to manually overwrite the absolute stock level (`availableQty`)
- **Request Body**:
  ```json
  {
    "availableQty": 150
  }
  ```
- **Response**: Returns the updated Inventory object
- **Status Codes**:
  - `200 OK`: Successfully updated
  - `404 NOT_FOUND`: Product not found
  - `400 BAD_REQUEST`: Invalid quantity (negative)
- **Validation**:
  - Quantity cannot be null
  - Quantity cannot be less than 0
  - Does NOT affect `reservedQty` or `soldQty`

### 2. **GET /api/inventory/low-stock** (JWT Auth Ready)
- **Description**: Returns all products where `availableQty < lowStockThreshold`
- **Response**: List of `LowStockResponse` objects with `productId` and `availableQty`
- **Response Example**:
  ```json
  [
    {
      "productId": "PROD-001",
      "availableQty": 10
    },
    {
      "productId": "PROD-002",
      "availableQty": 5
    }
  ]
  ```
- **Status Code**: `200 OK`

## Project Structure

### New Files Created
```
src/main/java/com/example/demo/
├── controllers/
│   ├── InventoryController.java (Updated with new endpoints)
│   └── dto/
│       ├── StockUpdateRequest.java (Input validation DTO)
│       └── LowStockResponse.java (Response DTO)
└── data/
    └── repositories/
        └── InventoryRepository.java (Updated with custom queries)

src/test/java/com/example/demo/
└── controllers/
    └── InventoryControllerTest.java (Comprehensive unit tests)
```

## Implementation Details

### InventoryRepository
Added two new methods:
```java
@Query("SELECT i FROM Inventory i WHERE i.availableQty < i.lowStockThreshold")
List<Inventory> findByAvailableQtyLessThanLowStockThreshold();

Optional<Inventory> findByProductId(String productId);
```

### DTOs
**StockUpdateRequest.java**:
- Validates that `availableQty` is not null and >= 0
- Uses Jakarta validation annotations

**LowStockResponse.java**:
- Simple DTO containing `productId` and `availableQty`

### InventoryController
New endpoints added:
- `updateAbsoluteStock(String productId, StockUpdateRequest request)`
- `getLowStockInventory()`

Both endpoints include proper error handling with `ErrorResponse` inner class.

## Unit Tests

Comprehensive test coverage in `InventoryControllerTest.java`:

### Tests for PUT endpoint:
- ✅ `testUpdateAbsoluteStock_Success`: Happy path - successful update
- ✅ `testUpdateAbsoluteStock_ProductNotFound`: 404 when product not found
- ✅ `testUpdateAbsoluteStock_NegativeQuantity`: 400 when quantity is negative
- ✅ `testUpdateAbsoluteStock_ZeroQuantity`: Valid update with quantity=0

### Tests for GET endpoint:
- ✅ `testGetLowStockInventory_Success`: Multiple low stock items
- ✅ `testGetLowStockInventory_Empty`: Empty list when no low stock items
- ✅ `testGetLowStockInventory_MultipleItems`: Correct count and data

### Regression Tests:
- ✅ `testGetAllInventory`: Existing functionality preserved
- ✅ `testGetInventoryById_Found`: Existing functionality preserved
- ✅ `testGetInventoryById_NotFound`: Existing functionality preserved

**All tests pass successfully** ✅

## Dependencies Added

Updated `build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
testImplementation 'org.mockito:mockito-inline:5.2.0'
```

## Build & Test Status

✅ **Compilation**: Successful (Java 21, Gradle 9.4.1)
✅ **Unit Tests**: All tests passing
✅ **No regressions**: Existing endpoints unaffected

## JWT Authentication Integration (Future)

The endpoints are designed to support JWT authentication. To enable:

1. Add `@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")` to endpoints
2. Implement JWT filter in the application
3. Add security configuration in `SecurityConfig` class

Example:
```java
@PutMapping("/{productId}/stock")
@PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
public ResponseEntity<?> updateAbsoluteStock(@PathVariable String productId, @Valid @RequestBody StockUpdateRequest request) {
    // Implementation
}
```

## Next Steps

1. Implement JWT security filter
2. Add role-based access control
3. Add integration tests with TestContainers
4. Implement audit logging for stock changes
5. Add metrics/monitoring for low-stock alerts

