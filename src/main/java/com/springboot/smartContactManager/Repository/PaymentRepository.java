package com.springboot.smartContactManager.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.smartContactManager.entities.PaymentData;



public interface PaymentRepository extends JpaRepository<PaymentData,Integer>{
    
    public PaymentData findByOrderId(String orderId);

}
