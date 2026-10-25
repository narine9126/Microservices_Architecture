package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final ProductClient productClient;
    private final OrderRepository orderRepository;

    public OrderService(ProductClient productClient, OrderRepository orderRepository) {
        this.productClient = productClient;
        this.orderRepository = orderRepository;
    }

    /**
     * Retry is useful for transient errors such as temporary network failures and timeouts.
     * Avoid blindly retrying non-retryable errors or non-idempotent operations, such as order creation that might create duplicate orders.
     * @param productId
     * @param quantity
     * @return
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "createOrderFallback")
    @Retry(name = "productService",fallbackMethod = "createOrderFallback")
    public Order createOrder(Long productId, Integer quantity) {
        // calling ProductService
        ProductClient.ProductResponse product = productClient.getProduct(productId);

        if (product.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock");
        }

        double totalPrice = product.getPrice() * quantity;

        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setStatus("CREATED");
        /*Order order = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .status("CREATED")
                .build();*/

        return orderRepository.save(order);
    }


    //fallBackMethod, this method execute when
    public Order createOrderFallback(Long productId, Integer quantity, Throwable t) {
        // We can store a FAILED order or return a dummy object
        /*Order failedOrder = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .status("FAILED")
                .totalPrice(0.0)
                .build();*/

        Order failedOrder = new Order();
        failedOrder.setProductId(productId);
        failedOrder.setQuantity(quantity);
        failedOrder.setTotalPrice(0.0);
        failedOrder.setStatus("FAILED");

        return orderRepository.save(failedOrder);
    }
}
