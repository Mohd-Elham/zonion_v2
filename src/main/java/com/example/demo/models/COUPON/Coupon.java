package com.example.demo.models.COUPON;

import com.example.demo.models.Users;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "Coupon")
public class Coupon {

    @Id
    private String id;
    private String couponCode;
    private String couponName;

    public String getCouponDescription() {
        return couponDescription;
    }

    public void setCouponDescription(String couponDescription) {
        this.couponDescription = couponDescription;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private String couponDescription;

    private DiscountType discountType;
    private BigDecimal discountValue;

    private Date expirationDate;
    private Date createdAt;

    private BigDecimal minimumPurchaseAmount;
    private BigDecimal maximumDiscountAmount;
    private Set<Users> usedUsersIds = new HashSet<>();
    private boolean isActive;

    public boolean getisActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public Coupon(String id, String couponCode, String couponName, DiscountType discountType, BigDecimal discountValue, Date expirationDate, Date createdAt,
                  BigDecimal minimumPurchaseAmount, BigDecimal maximumDiscountAmount, Set<Users> usedUsersIds, boolean isActive) {
        this.id = id;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.minimumPurchaseAmount = minimumPurchaseAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.usedUsersIds = usedUsersIds;
        this.isActive = isActive;
    }

    public BigDecimal getMaximumDiscountAmount() {
        return maximumDiscountAmount;
    }

    public void setMaximumDiscountAmount(BigDecimal maximumDiscountAmount) {
        this.maximumDiscountAmount = maximumDiscountAmount;
    }

    public Coupon() {
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "id='" + id + '\'' +
                ", couponCode='" + couponCode + '\'' +
                ", couponName='" + couponName + '\'' +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", expirationDate=" + expirationDate +
                ", createdAt=" + createdAt +
                ", minimumPurchaseAmount=" + minimumPurchaseAmount +
                ", maximumDiscountAmount=" + maximumDiscountAmount +
                ", usedUsersIds=" + usedUsersIds +
                ", isActive=" + isActive +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getMinimumPurchaseAmount() {
        return minimumPurchaseAmount;
    }

    public void setMinimumPurchaseAmount(BigDecimal minimumPurchaseAmount) {
        this.minimumPurchaseAmount = minimumPurchaseAmount;
    }

    public Set<Users> getUsedUsersIds() {
        return usedUsersIds;
    }

    public void setUsedUsersIds(Set<Users> usedUsersIds) {
        this.usedUsersIds = usedUsersIds;
    }
}
