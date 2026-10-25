# Create a product via API Gateway :: 
curl -X POST "http://localhost:8080/api/products" \
-H "Content-Type: application/json" \
-d '{
"name": "Laptop",
"price": 75000,
"availableQuantity": 10
}'

# List products
curl "http://localhost:8080/api/products"

# Create an order
curl -X POST "http://localhost:8080/api/orders?productId=1&quantity=2"

# Health Check for Order-Service
curl http://localhost:8082/actuator/health 