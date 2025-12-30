package com.example.demo.service;

import com.example.demo.models.Wishlists;
import com.example.demo.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public void toggleProductInWishlist(String userId, String productId) {
        Optional<Wishlists> wishlistOpt = wishlistRepository.findByUserId(userId);

        if (wishlistOpt.isPresent()) {
            Wishlists wishlist = wishlistOpt.get();
            List<String> productIds = wishlist.getProductId();

            if (productIds.contains(productId)) {
                productIds.remove(productId);
            } else {
                productIds.add(productId);
            }
            wishlistRepository.save(wishlist);
        } else {
            List<String> newList = new ArrayList<>();
            String randUUiD = java.util.UUID.randomUUID().toString();
            newList.add(productId);
            Wishlists newWishlist = new Wishlists(randUUiD, newList, userId);
            wishlistRepository.save(newWishlist);
        }
    }

    public List<String> getWishlistProductIds(String userId) {
        return wishlistRepository.findByUserId(userId)
                .map(Wishlists::getProductId)
                .orElse(new ArrayList<>());
    }

    public Wishlists getWishlistByUserId(String userId) {
        return wishlistRepository.findByUserId(userId).orElse(null);
    }
}
