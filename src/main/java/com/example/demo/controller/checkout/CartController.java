package com.example.demo.controller.checkout;

import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.Cart;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.example.demo.service.checkout.CartService;
import com.example.demo.service.UserService;
import com.example.demo.service.profile.ProductService;
import com.example.demo.service.profile.addressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final com.example.demo.service.profile.addressService addressService;
    private final AuthProviderFinder authProviderFinder;

    public CartController(CartService cartService, UserService userService, ProductService productService, addressService addressService, AuthProviderFinder authProviderFinder) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
        this.addressService = addressService;
        this.authProviderFinder = authProviderFinder;
    }

//    @GetMapping("")
//    public String cart(ModelMap map, Authentication auth) {
//        String username = auth.getName();
//        Users user = userService.findByUsername(username);
//        Cart cart = cartService.getCart(user.getId());
////
////        List<Products> productsInCart =
//        System.out.println(cart.getItems());
//        List<Cart.CartItems> cartItems = cart.getItems();
//        List<String> productsIds = cartItems.stream()
//                        .map(productId -> productId.getProductId())
//                                .toList();
//        List<Products> productsExsistingInCart = productService.getProductsByIds(productsIds);
//        map.addAttribute("cart", cart);
//
//
//        return "main/cart/cart_page";
//    }

@GetMapping("")
public String cart(ModelMap map, Authentication auth) {
    // Retrieve the username from the Authentication object
    Users username = authProviderFinder.getUserAuth(auth);
    System.out.println("DEBUG: Authenticated username: " + username);

    // Retrieve the user details from the userService
    Users user = userService.findByUsername(username.getUsername());
    if (user == null) {
        System.out.println("DEBUG: No user found for username: " + username);
        // You might want to handle this case appropriately
    } else {
        System.out.println("DEBUG: Retrieved user: " + user);
    }

    // Retrieve the cart for the user using their ID
    Cart cart = cartService.getCart(user.getId());
    if (cart == null) {
        System.out.println("DEBUG: No cart found for user id: " + user.getId());
        // Handle the case when the cart is not found if necessary
    } else {
        System.out.println("DEBUG: Retrieved cart: " + cart.toString());
    }

    // Retrieve and log the cart items
    List<Cart.CartItems> cartItems = cart.getItems();
    if (cartItems == null || cartItems.isEmpty()) {
        System.out.println("DEBUG: No items in cart for user id: " + user.getId());
    } else {
        System.out.println("DEBUG: Cart items: " + cartItems.toString());
    }

    // Extract product IDs from cart items and log each one
    List<String> productsIds = cartItems.stream()
            .map(item -> {
                String productId = item.getProductId();
                System.out.println("DEBUG: Processing product id: " + productId);
                return productId;
            })
            .toList();
    System.out.println("DEBUG: Extracted product IDs: " + productsIds);



    // Retrieve products from the productService using the extracted product IDs
    List<Products> productsExistingInCart = productService.getProductsByIds(productsIds);
    System.out.println("DEBUG: Retrieved products for cart: " + productsExistingInCart.toString());

    Map<String, Products> productsMap = productsExistingInCart.stream()
            .collect(Collectors.toMap(Products::getId, product -> product));

    productService.testProducts();
    // Add the cart to the model for the view
    map.addAttribute("cart", cart);
    map.addAttribute("productsMap", productsMap);

    return "main/cart/cart_page";
}


    @PostMapping("/add")
    public String addCart(
            @RequestParam("productId") String productId,
            @RequestParam("quantity") int quantity,
            @RequestParam("userId") String userId
    ) {

        System.out.println("THIS IS ADD CART");
        System.out.println("The incoming product id is: " + productId);
        System.out.println("The incoming quantity is: " + quantity);
        System.out.println("The incoming user id is: " + userId);

        Products product = productService.getProductById(productId);
        int stock = product.getStockQuantityInt();

        // Validate quantity against stock
        int adjustedQuantity = Math.min(quantity, stock);
        if(adjustedQuantity < 1) adjustedQuantity = 1;

        cartService.addToCart(productId, adjustedQuantity, userId);

        if (product.getStockQuantityInt() > 1) {
            return "redirect:/product/" + productId;
        }else{
            return "redirect:/";
        }

    }

    @PostMapping("/update/quantity")
    public String updateCart(
            @RequestParam("productId") String productId,
            @RequestParam("quantity") int quantity,
            Authentication auth) {
        // Get the logged in user details
        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users user = userService.findByUsername(currentUser.getUsername());

        System.out.println("DEBUG: Authenticated username: " + currentUser.getUsername());
        System.out.println("The incoming product id is: " + productId);
        System.out.println("The incoming quantity is: " + quantity);

        Products product = productService.getProductById(productId);
        int stock = product.getStockQuantityInt();

        // Validate quantity against stock
        int adjustedQuantityy = Math.min(quantity, stock);
        if(adjustedQuantityy < 1) adjustedQuantityy = 1;

        // Update the product quantity in the user's cart
        cartService.updateQuantity(user.getId(), productId, adjustedQuantityy);

        // Redirect back to the cart page after updating
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam("productId") String productId,
            Authentication auth) {
        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users user = userService.findByUsername(currentUser.getUsername());

        cartService.removeFromCart(user.getId(), productId);

        return "redirect:/cart";
    }

}
