package com.trash.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trash.ecommerce.entity.PaymentMethod;
import java.util.Optional;


@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByMethodName(String methodName);
}
