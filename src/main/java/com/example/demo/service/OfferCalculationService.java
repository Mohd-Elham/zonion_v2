package com.example.demo.service;


import com.example.demo.models.COUPON.DiscountType;
import com.example.demo.models.Cart;
import com.example.demo.models.Products;
import com.example.demo.models.offer.Offer;
import com.example.demo.models.offer.OfferType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class OfferCalculationService {
    @Autowired
//    private static final Logger logger = (Logger) LoggerFactory.getLogger(OfferCalculationService.class);

    private final OfferService offerService;

    @Autowired
    public OfferCalculationService(OfferService offerService) {
        this.offerService = offerService;
    }

    public void applyOffers(Cart cart, Map<String, Products> productsMap) {
        List<Offer> activeOffers = offerService.findActiveOffers();
        System.out.println("Active offers: " + activeOffers);
        Map<String, String> appliedOffers = new HashMap<>();
        double totalOfferDiscount = 0.0;

        for (Cart.CartItems item : cart.getItems()) {
            Products product = productsMap.get(item.getProductId());
            if (product == null) continue;

            BigDecimal itemTotal = BigDecimal.valueOf(item.getPrice() * item.getQuantity());
            System.out.println("Total no of items: " + itemTotal.toString());
            // 1. Check Product-specific offers
            Offer bestOffer = activeOffers.stream()
                    .filter(o -> {
                        // Check product offers
                        boolean isProductOffer = o.getOfferType() == OfferType.PRODUCT_OFFER &&
                                o.getProducts().stream()
                                        .anyMatch(p -> p.getId().equals(product.getId()));

                        // Check category offers
                        boolean isCategoryOffer = o.getOfferType() == OfferType.CATEGORY_OFFER &&
                                product.getCategory() != null &&
                                o.getCategories().stream()
                                        .anyMatch(c -> c.getId().equals(product.getCategory()));

                        return isProductOffer || isCategoryOffer;
                    })
                    .findFirst()
                    .orElse(null);

            if (bestOffer != null) {
                System.out.println("Best offer: " + bestOffer.toString());
            } else {
                System.out.println("No applicable offer found for product: " + product.getId());
            }
//            System.out.println("Best offer: " + bestOffer.toString());
            if (bestOffer != null) {
                BigDecimal discount = calculateDiscount(bestOffer, itemTotal);
                totalOfferDiscount += discount.doubleValue();

                // Record which offer was applied to which product
                appliedOffers.put(product.getId(), bestOffer.getId());
                System.out.println("Applied: " + appliedOffers.get(product.getId()));

//                logger.info("Applied {"+bestOffer.getOfferType()+"} offer {"+ bestOffer.getId()+"} to product {"+product.getId()+"}: -₹{"+discount+"}");
            }
        }

        // Update cart values
        System.out.println("Applied offer discount: " + totalOfferDiscount);
        cart.setOfferDiscountedValue(totalOfferDiscount);
        System.out.println("Applied offer discount: " + appliedOffers);
        cart.setAppliedOffers(appliedOffers);
//        cart.setTotalPrice(cart.getTotalPrice() - totalOfferDiscount);

    }

    public BigDecimal calculateDiscount(Offer offer, BigDecimal itemTotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = itemTotal.multiply(offer.getDiscountValue().divide(BigDecimal.valueOf(100)));
        } else {
            discount = offer.getDiscountValue();
        }

        // Apply constraints
        if (offer.getMinimumDiscountAmount() != null) {
            discount = discount.max(offer.getMinimumDiscountAmount());
        }
        if (offer.getMaximumDiscountAmount() != null) {
            discount = discount.min(offer.getMaximumDiscountAmount());
        }
        System.out.println("THE DISCOUNT: " + discount);
        return discount;
    }
}
