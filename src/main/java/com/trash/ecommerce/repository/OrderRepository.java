package com.trash.ecommerce.repository;

import com.trash.ecommerce.dto.OrderSummaryDTO;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trash.ecommerce.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreateAtDesc(Long userId);
    
    @Query("SELECT COUNT(o) > 0 FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE o.user.id = :userId " +
           "AND oi.product.id = :productId " +
           "AND o.status IN (com.trash.ecommerce.entity.OrderStatus.PAID, com.trash.ecommerce.entity.OrderStatus.PLACED)")
    boolean existsByUserIdAndProductIdAndStatusPaid(@Param("userId") Long userId, @Param("productId") Long productId);
}
