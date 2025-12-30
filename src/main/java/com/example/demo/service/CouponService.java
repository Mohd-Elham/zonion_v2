package com.example.demo.service;

import com.example.demo.models.COUPON.Coupon;
import com.example.demo.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    private CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    public void save(Coupon coupon) {
        couponRepository.save(coupon);
    }

    public Coupon findById(String id) {
        return couponRepository.findById(id).orElse(null);
    }

    public void toggleCoupon(Coupon coupon) {
        coupon.setIsActive(!coupon.getisActive());
        couponRepository.save(coupon);
    }

    public Coupon findByCouponCode(String couponCode) {
        Optional<Coupon> coupon = couponRepository.findByCouponCode(couponCode);
        return coupon.orElse(null);
    }
}
