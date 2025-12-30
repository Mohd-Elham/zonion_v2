package com.example.demo.service.checkout;

import com.example.demo.models.Cart;
import com.example.demo.models.Products;
import com.example.demo.repository.MyCartRepository;
import com.example.demo.repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {


    private final MyCartRepository cartRepo;
    private final ProductsRepository productsRepository;

    public CartService(MyCartRepository cartRepo, ProductsRepository productsRepository) {
        this.cartRepo = cartRepo;
        this.productsRepository = productsRepository;
    }

    public void addToCart(String productId, int quantity, String userId) {
        Products existingProduct = productsRepository.findById(productId).orElseThrow( () -> new RuntimeException("Product not found") );
            Double price = Double.parseDouble(existingProduct.getPrice());



        Optional<Cart> exsistingCart = cartRepo.findByUserId(userId);

        if(exsistingCart.isPresent()) {
            Cart cart = exsistingCart.get();
            List<Cart.CartItems> items = cart.getItems(); // we got all the items
            Optional<Cart.CartItems> exsistingCartItem = items.stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst(); //then we found the cart with the perticular product for increasing the quantity
            if(exsistingCartItem.isPresent()) {
                exsistingCartItem.get().setQuantity(exsistingCartItem.get().getQuantity() + quantity); //increamenting quantity
            } else {
                items.add(new Cart.CartItems(productId, quantity, price));//if product is not available add the item
            }
            double totalPrice = calculateTotalPrice(items);
            cart.setTotalPrice(totalPrice);
            cartRepo.save(cart);
//            blah blah
        } else {
            String cartId = UUID.randomUUID().toString();
            List<Cart.CartItems> cartItems = new ArrayList<>();

            cartItems.add(new Cart.CartItems(productId, quantity, price));
            double totalPrice = calculateTotalPrice(cartItems);
            Cart newCart = new Cart(cartId, userId, cartItems,totalPrice);
            cartRepo.save(newCart);
        }

    }


    private double calculateTotalPrice(List<Cart.CartItems> items) {
        return items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    }

    public Cart getCart(String id) {
        return cartRepo.findByUserId(id).orElse(new Cart(UUID.randomUUID().toString(), id, new ArrayList<>(), 0.0));
    }

    public void updateQuantity(String id, String productId, int newQuantity) {
        // Optional: Ensure newQuantity is within allowed limits (1 to 5)
//        newQuantity = Math.max(1, Math.min(newQuantity, 5));

        // Fetch the cart for the user
        Cart cart = cartRepo.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        System.out.println("Item product id: " + cart.getItems());
        System.out.println("INCOMING PRODUCT ID: " + productId);

        // Update the quantity for the matching product
        for (Cart.CartItems item : cart.getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(newQuantity);
                System.out.println("Quantity updated is " + item.getQuantity());
                break;
            }
        }

        // Recalculate the total price
        double totalPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);

        // Save the updated cart
        cartRepo.save(cart);
    }

    public void removeFromCart(String userId, String productId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<Cart.CartItems> items = cart.getItems();
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));

        if (removed) {
            // Recalculate total price
            double newTotal = items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            cart.setTotalPrice(newTotal);
            cart.setCouponDiscountedValue(0);
            cart.setOfferDiscountedValue(0);
            cartRepo.save(cart);
        }
    }
}
