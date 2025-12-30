package com.example.demo.repository;

import com.example.demo.models.COUPON.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CouponRepository extends MongoRepository<Coupon, String> {
    Optional<Coupon> findByCouponCode(String couponCode);
}
