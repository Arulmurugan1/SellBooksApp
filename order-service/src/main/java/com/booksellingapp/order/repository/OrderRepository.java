package com.booksellingapp.order.repository;

import com.booksellingapp.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    java.util.List<Order> findByCustomerId(String customerId);
}
