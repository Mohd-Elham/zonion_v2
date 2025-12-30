package com.example.demo.models.offer;

import com.example.demo.models.COUPON.DiscountType;
import com.example.demo.models.Category;
import com.example.demo.models.Products;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Offer {
    @Id
    private String id;
    private String offerName;
    private String offerDescription;
    private OfferType offerType;

    private Set<Products> products;
    private Set<Category> categories;

    private DiscountType discountType;
    private BigDecimal discountValue;

    private BigDecimal minimumDiscountAmount;
    private BigDecimal maximumDiscountAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private BigDecimal offerDiscountAmount;
    private boolean active;

    public Offer(String id, String offerName, String offerDescription, OfferType offerType, Set<Products> products,
                 Set<Category> categories, DiscountType discountType, BigDecimal discountValue, BigDecimal minimumDiscountAmount,
                 BigDecimal maximumDiscountAmount, LocalDateTime startDate, LocalDateTime endDate, BigDecimal offerDiscountAmount, boolean active) {
        this.id = id;
        this.offerName = offerName;
        this.offerDescription = offerDescription;
        this.offerType = offerType;
        this.products = products;
        this.categories = categories;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumDiscountAmount = minimumDiscountAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.offerDiscountAmount = offerDiscountAmount;
        this.active = active;
    }


    @Override
    public String toString() {
        return "Offer{" +
                "id='" + id + '\'' +
                ", offerName='" + offerName + '\'' +
                ", offerDescription='" + offerDescription + '\'' +
                ", offerType=" + offerType +
                ", products=" + products +
                ", categories=" + categories +
                ", discountType=" + discountType +
                ", discountValue=" + discountValue +
                ", minimumDiscountAmount=" + minimumDiscountAmount +
                ", maximumDiscountAmount=" + maximumDiscountAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", offerDiscountAmount=" + offerDiscountAmount +
                ", active=" + active +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOfferName() {
        return offerName;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public OfferType getOfferType() {
        return offerType;
    }

    public void setOfferType(OfferType offerType) {
        this.offerType = offerType;
    }

    public Set<Products> getProducts() {
        return products;
    }

    public void setProducts(Set<Products> products) {
        this.products = products;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
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

    public BigDecimal getMinimumDiscountAmount() {
        return minimumDiscountAmount;
    }

    public void setMinimumDiscountAmount(BigDecimal minimumDiscountAmount) {
        this.minimumDiscountAmount = minimumDiscountAmount;
    }

    public BigDecimal getMaximumDiscountAmount() {
        return maximumDiscountAmount;
    }

    public void setMaximumDiscountAmount(BigDecimal maximumDiscountAmount) {
        this.maximumDiscountAmount = maximumDiscountAmount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getOfferDiscountAmount() {
        return offerDiscountAmount;
    }

    public void setOfferDiscountAmount(BigDecimal offerDiscountAmount) {
        this.offerDiscountAmount = offerDiscountAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }
}
