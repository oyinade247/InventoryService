# Seller Stock Management APIs - API Documentation

## Base URL
```
http://localhost:8082/api/inventory
```

---

## Endpoints

### 1. Update Absolute Stock Level

**Endpoint:** `PUT /api/inventory/{productId}/stock`

**Description:** Allows a seller to manually overwrite the absolute stock level (availableQty) for a specific product. This does NOT affect reserved or sold quantities.

**Authentication:** JWT Required (ROLE_SELLER or ROLE_ADMIN)

#### Request

**Path Parameters:**
- `productId` (string, required): The product ID to update (e.g., "PROD-001")

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Body:**
```json
{
  "availableQty": 150
}
```

**Validation Rules:**
- `availableQty` cannot be null
- `availableQty` must be >= 0 (no negative values)

#### Responses

**Success (200 OK):**
```json
{
  "id": "1",
  "productId": "PROD-001",
  "availableQty": 150,
  "reservedQty": 20,
  "soldQty": 30,
  "lowStockThreshold": 25,
  "createdAt": "2026-04-20T10:30:00",
  "updatedAt": "2026-04-20T11:45:00"
}
```

**Product Not Found (404):**
```json
{
  "message": "Product not found: PROD-999"
}
```

**Invalid Input (400):**
```json
{
  "message": "Available quantity cannot be less than 0"
}
```

**Unauthorized (401):**
```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

**Forbidden (403):**
```json
{
  "error": "Access Denied",
  "message": "You do not have permission to perform this action"
}
```

#### Example Requests

**Using cURL:**
```bash
curl -X PUT http://localhost:8082/api/inventory/PROD-001/stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"availableQty": 150}'
```

**Using Postman:**
1. Set Method to `PUT`
2. Set URL to `http://localhost:8082/api/inventory/PROD-001/stock`
3. Add Header: `Authorization: Bearer <your_jwt_token>`
4. Set Body to raw JSON:
```json
{
  "availableQty": 150
}
```

**Using JavaScript/Fetch:**
```javascript
const response = await fetch('http://localhost:8082/api/inventory/PROD-001/stock', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + jwtToken
  },
  body: JSON.stringify({
    availableQty: 150
  })
});

const result = await response.json();
console.log(result);
```

---

### 2. Get Low Stock Inventory

**Endpoint:** `GET /api/inventory/low-stock`

**Description:** Retrieves all products where the available quantity is strictly less than the low stock threshold. Returns product IDs and their current available quantities.

**Authentication:** JWT Required (ROLE_SELLER or ROLE_ADMIN)

#### Request

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

#### Responses

**Success (200 OK):**
```json
[
  {
    "productId": "PROD-001",
    "availableQty": 10
  },
  {
    "productId": "PROD-002",
    "availableQty": 5
  },
  {
    "productId": "PROD-003",
    "availableQty": 15
  }
]
```

**Empty List (200 OK):**
```json
[]
```

**Unauthorized (401):**
```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

**Forbidden (403):**
```json
{
  "error": "Access Denied",
  "message": "You do not have permission to perform this action"
}
```

#### Example Requests

**Using cURL:**
```bash
curl -X GET http://localhost:8082/api/inventory/low-stock \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Using Postman:**
1. Set Method to `GET`
2. Set URL to `http://localhost:8082/api/inventory/low-stock`
3. Add Header: `Authorization: Bearer <your_jwt_token>`
4. Click Send

**Using JavaScript/Fetch:**
```javascript
const response = await fetch('http://localhost:8082/api/inventory/low-stock', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + jwtToken
  }
});

const lowStockItems = await response.json();
console.log(lowStockItems);
```

**Using Python/Requests:**
```python
import requests

headers = {
    'Authorization': f'Bearer {jwt_token}'
}

response = requests.get('http://localhost:8082/api/inventory/low-stock', headers=headers)
low_stock_items = response.json()
print(low_stock_items)
```

---

## Data Models

### Inventory (Response Object)
```json
{
  "id": "string (UUID)",
  "productId": "string",
  "availableQty": "integer",
  "reservedQty": "integer",
  "soldQty": "integer",
  "lowStockThreshold": "integer",
  "createdAt": "datetime (ISO-8601)",
  "updatedAt": "datetime (ISO-8601)"
}
```

### StockUpdateRequest (Request Body)
```json
{
  "availableQty": "integer (required, >= 0)"
}
```

### LowStockResponse (Response Object)
```json
{
  "productId": "string",
  "availableQty": "integer"
}
```

### ErrorResponse (Error Response Body)
```json
{
  "message": "string describing the error"
}
```

---

## HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid input (e.g., negative quantity) |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Product not found |
| 500 | Internal Server Error | Server error |

---

## Authentication

All endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <JWT_TOKEN>
```

The JWT token must contain one of the following roles:
- `ROLE_SELLER`
- `ROLE_ADMIN`

---

## Business Rules

### PUT /api/inventory/{productId}/stock
1. Only updates `availableQty`, does not affect `reservedQty` or `soldQty`
2. Quantity cannot be negative (must be >= 0)
3. Product must exist in the database
4. Changes are immediately persisted
5. Updates the `updatedAt` timestamp

### GET /api/inventory/low-stock
1. Returns only products where `availableQty < lowStockThreshold`
2. Comparison is strict less-than (not less-than-or-equal)
3. Returns empty list if no products are below threshold
4. Results are not paginated (all matching items returned)

---

## Error Handling

### Validation Errors
If the request body contains invalid data:
```json
{
  "message": "Available quantity cannot be null"
}
```

### Not Found Errors
If the product does not exist:
```json
{
  "message": "Product not found: PROD-999"
}
```

### Business Logic Errors
If quantity is negative:
```json
{
  "message": "Available quantity cannot be less than 0"
}
```

---

## Rate Limiting

Currently, there are no rate limits implemented. This should be considered for production deployments.

---

## Versioning

Current API Version: **1.0**

Future versions may be introduced with `/api/v2/inventory` paths.

---

## Support

For issues or questions about these APIs, contact the development team.

