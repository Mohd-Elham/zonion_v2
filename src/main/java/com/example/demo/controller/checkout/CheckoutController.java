    package com.example.demo.controller.checkout;

    import com.example.demo.config.AuthProviderFinder;
    import com.example.demo.models.*;
    import com.example.demo.models.COUPON.Coupon;
    import com.example.demo.models.COUPON.DiscountType;
    import com.example.demo.models.wallet.TransactionHistory;
    import com.example.demo.models.wallet.TransactionStatus;
    import com.example.demo.models.wallet.Wallet;
    import com.example.demo.repository.*;
    import com.example.demo.service.*;
    import com.example.demo.service.checkout.CartService;
    import com.example.demo.service.checkout.OrderService;
    import com.example.demo.service.profile.ProductService;
    import com.example.demo.service.profile.addressService;
    import com.razorpay.Payment;
    import com.razorpay.RazorpayClient;
    import com.razorpay.RazorpayException;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    import org.thymeleaf.TemplateEngine;
    import org.thymeleaf.context.Context;
    import org.xhtmlrenderer.pdf.ITextRenderer;

    import java.io.ByteArrayOutputStream;
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.function.Function;
    import java.util.stream.Collectors;

    @Controller
    public class CheckoutController {

        private final com.example.demo.service.profile.addressService addressService;
        private final UserService userService;
        private final MyCartRepository myCartRepository;
        private final ProductsRepository productsRepository;
        private final OrderService orderService;
        private final ProductService productService;
        private final AuthProviderFinder authProviderFinder;
        private final CouponService couponService;
        private final CouponRepository couponRepository;
        private final CartService cartService;
        private final OfferCalculationService offerCalculationService;
        private final RazorPayService razorPayService;
        private final WalletService walletService;
        private final TransactionHistoryRepository transactionHistoryRepository;
        private final WalletRepository walletRepository;
        @Autowired
        private OrderRepository orderRepository;


        @Autowired
        public CheckoutController(addressService addressService, UserService userService, MyCartRepository myCartRepository, ProductsRepository productsRepository, OrderService orderService, ProductService productService, AuthProviderFinder authProviderFinder, CouponService couponService, CouponRepository couponRepository, CartService cartService, OfferCalculationService offerCalculationService, RazorPayService razorPayService, WalletService walletService, TransactionHistoryRepository transactionHistoryRepository, WalletRepository walletRepository) {
            this.addressService = addressService;
            this.userService = userService;
            this.myCartRepository = myCartRepository;
            this.productsRepository = productsRepository;
            this.orderService = orderService;
            this.productService = productService;
            this.authProviderFinder = authProviderFinder;
            this.couponService = couponService;
            this.couponRepository = couponRepository;
            this.cartService = cartService;
            this.offerCalculationService = offerCalculationService;
            this.razorPayService = razorPayService;
            this.walletService = walletService;
            this.transactionHistoryRepository = transactionHistoryRepository;
            this.walletRepository = walletRepository;
        }

        @GetMapping("/checkout")
        public String checkout(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
            Users currentUser = authProviderFinder.getUserAuth(authentication);
            Users user = userService.findByUsername(currentUser.getUsername());

            List<Address> addresses = addressService.getAddressesByUserId(user.getId());
            model.addAttribute("addresses", addresses);

            Optional<Cart> cartOpt = myCartRepository.findByUserId(user.getId());

            if (cartOpt.isEmpty()) {
                return "redirect:/cart?error=no_cart";
            }

            Cart cart = cartOpt.get();

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return "redirect:/cart?error=empty";
            }

            // Retrieve product details
            List<String> productIds = cart.getItems().stream()
                    .map(Cart.CartItems::getProductId)
                    .collect(Collectors.toList());
            Map<String, Products> productsMap = productsRepository.findAllById(productIds)
                    .stream()
                    .collect(Collectors.toMap(Products::getId, Function.identity()));

            // Check stock for each item
            for (Cart.CartItems item : cart.getItems()) {
                Products product = productsMap.get(item.getProductId());
                if (product == null) {
                    redirectAttributes.addFlashAttribute("error", "Product not available: " + item.getProductId());
                    return "redirect:/cart";
                }
                int stock = product.getStockQuantityInt();
                if (item.getQuantity() > stock) {
                    redirectAttributes.addFlashAttribute("error", "Insufficient stock for " + product.getProduct_name() + ". Available: " + stock);
                    return "redirect:/cart";
                }
            }

            // Apply offers and proceed
            offerCalculationService.applyOffers(cart, productsMap);
            myCartRepository.save(cart);

            Wallet wallet = walletRepository.findByUserId(user.getId());
            if (wallet == null) {
                wallet = new Wallet();
                String randUUID = java.util.UUID.randomUUID().toString();
                wallet.setWalletId(randUUID);
                wallet.setUserId(user.getId());
                wallet.setBalance(0.0);
                wallet = walletRepository.save(wallet);
            }
            double balance = wallet.getBalance();

//            cart.setCouponCode(null);
//            cart.setCouponDiscountedValue(0);
//            cart.setTotalPrice(cart.getTotalPrice() + cart.getCouponDiscountedValue());
//            myCartRepository.save(cart);

            model.addAttribute("productsMap", productsMap);
            model.addAttribute("userId", user.getId());
            model.addAttribute("discountedAmount", cart.getTotalPrice() - cart.getOfferDiscountedValue() - cart.getCouponDiscountedValue());
            model.addAttribute("cart", cart);
            model.addAttribute("balance", balance);

            return "checkout/checkout_page";
        }

        @GetMapping("/checkout/retry/{id}")
        public String checkoutRetry(@PathVariable("id") String orderId,Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
            Users currentUser = authProviderFinder.getUserAuth(authentication);
            Users user = userService.findByUsername(currentUser.getUsername());



            Orders existingOrders = orderRepository.findById(orderId).get();

            orderRepository.delete(existingOrders);
            List<Address> addresses = addressService.getAddressesByUserId(user.getId());
            model.addAttribute("addresses", addresses);

            Optional<Cart> cartOpt = myCartRepository.findByUserId(user.getId());

            if (cartOpt.isEmpty()) {
                return "redirect:/cart?error=no_cart";
            }

            Cart cart = cartOpt.get();

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return "redirect:/cart?error=empty";
            }

            // Retrieve product details
            List<String> productIds = cart.getItems().stream()
                    .map(Cart.CartItems::getProductId)
                    .collect(Collectors.toList());
            Map<String, Products> productsMap = productsRepository.findAllById(productIds)
                    .stream()
                    .collect(Collectors.toMap(Products::getId, Function.identity()));

            // Check stock for each item
            for (Cart.CartItems item : cart.getItems()) {
                Products product = productsMap.get(item.getProductId());
                if (product == null) {
                    redirectAttributes.addFlashAttribute("error", "Product not available: " + item.getProductId());
                    return "redirect:/cart";
                }
                int stock = product.getStockQuantityInt();
                if (item.getQuantity() > stock) {
                    redirectAttributes.addFlashAttribute("error", "Insufficient stock for " + product.getProduct_name() + ". Available: " + stock);
                    return "redirect:/cart";
                }
            }


            // Apply offers and proceed
            offerCalculationService.applyOffers(cart, productsMap);
            myCartRepository.save(cart);

            Wallet wallet = walletRepository.findByUserId(user.getId());
            if (wallet == null) {
                wallet = new Wallet();
                String randUUID = java.util.UUID.randomUUID().toString();
                wallet.setWalletId(randUUID);
                wallet.setUserId(user.getId());
                wallet.setBalance(0.0);
                wallet = walletRepository.save(wallet);
            }
            double balance = wallet.getBalance();

//            cart.setCouponCode(null);
//            cart.setCouponDiscountedValue(0);
//            cart.setTotalPrice(cart.getTotalPrice() + cart.getCouponDiscountedValue());
//            myCartRepository.save(cart);

            model.addAttribute("productsMap", productsMap);
            model.addAttribute("userId", user.getId());
            model.addAttribute("discountedAmount", cart.getTotalPrice() - cart.getOfferDiscountedValue() - cart.getCouponDiscountedValue());
            model.addAttribute("cart", cart);
            model.addAttribute("balance", balance);

            return "checkout/checkout_page";
        }

        @GetMapping("/checkout/remove-coupon/{cartId}")
        public String removeCoupon(@PathVariable("cartId") String cartId, Authentication authentication, Model model) {
            Optional<Cart> cartOpt = myCartRepository.findById(cartId);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                cart.setCouponCode(null);  // Alternatively, cart.setCouponCode(null);
                cart.setCouponDiscountedValue(0);
                cart.setTotalPrice(cart.getTotalPrice() + cart.getCouponDiscountedValue());
                myCartRepository.save(cart);
            }

            return "redirect:/checkout";

        }

        @PostMapping("/checkout/placeholder")
        public String placeOrder(@RequestParam("selectedAddress") String selectedAddress,
                                 @RequestParam("paymentMethod") PaymentType paymentMethod,
                                 @RequestParam(name = "couponCode",required = false, defaultValue = "") String couponCode,
                                 Authentication authentication, Model model , RedirectAttributes redirectAttributes) {
            System.out.println("Coupon code coming from checkoiut is "+ couponCode);
            System.out.println("selectedAddress: " + selectedAddress
            + "paymentMethod: " + paymentMethod );

            Users currentUser = authProviderFinder.getUserAuth(authentication);
            String userId = userService.findByUsername(currentUser.getUsername()).getId();

            System.out.println("selectedAddress: " + selectedAddress);

            Cart cart = myCartRepository.findByUserId(userId).orElse(null);
            if (cart == null || cart.getItems().isEmpty()) {
                return "redirect:/cart";
            }

            // Create the order
            Orders order = orderService.setNewOrder(userId, selectedAddress, paymentMethod, cart);



            // Reduce stock for each product in the cart
            for (Cart.CartItems item : cart.getItems()) {
                String productId = item.getProductId();
                int quantity = item.getQuantity();
                Products product = productService.getProductById(productId);
                if (product != null) {
                    int currentStock = product.getStockQuantityInt();
                    int newStock = currentStock - quantity;
                    product.setStock_quantity(String.valueOf(newStock));
                    productService.saveProduct(product);
                }
            }



            if (!couponCode.isEmpty()) {
                Coupon coupon = couponService.findByCouponCode(couponCode);
                if (coupon != null) {
                    Set<Users> usedUsers = coupon.getUsedUsersIds();
                    usedUsers.add(currentUser);
                    coupon.setUsedUsersIds(usedUsers);
                    couponService.save(coupon);
                }
            }



            // Save offer information in order
            order.setOfferDiscount(cart.getOfferDiscountedValue());
            order.setCouponDiscount(cart.getCouponDiscountedValue());
            order.setAppliedOffers(cart.getAppliedOffers());


            // Clear the user's cart
            cart.getItems().clear();
            cart.setTotalPrice(0.0);
            cart.setCouponDiscountedValue(0.0);
            cart.setCouponCode(null);
            myCartRepository.save(cart);

            return "redirect:/order-confirmation/"+order.getId();
        }


        @PostMapping("/checkout/placeholder/razorpay")
        @ResponseBody
        public ResponseEntity<Map<String, String>> placeOrderRazorpay(
                @RequestBody Map<String, String> incomingetRequest,
//                                @RequestParam("razorpayId") String razorpayId,
//                                @RequestParam("selectedAddress") String selectedAddress,
//                                 @RequestParam("paymentMethod") PaymentType paymentMethod,
//                                 @RequestParam(name = "couponCode",required = false, defaultValue = "") String couponCode,
                                 Authentication authentication, Model model , RedirectAttributes redirectAttributes) {

            try {

                System.out.println("INSIDE PLACE ORDER RAZORPAY");
                System.out.println("Incoming request is " + incomingetRequest);

                String selectedAddress = incomingetRequest.get("selectedAddress");
                PaymentType paymentMethod = PaymentType.valueOf(incomingetRequest.get("paymentMethod"));
                String razorpayId = incomingetRequest.get("razorpayId");
                String couponCode = incomingetRequest.get("couponCode");
                System.out.println("Coupon code coming from checkoiut is " + couponCode);
                System.out.println("selectedAddress: " + selectedAddress
                        + "paymentMethod: " + paymentMethod);
//
                Users currentUser = authProviderFinder.getUserAuth(authentication);
                String userId = userService.findByUsername(currentUser.getUsername()).getId();
//
                System.out.println("selectedAddress: " + selectedAddress);

                Cart cart = myCartRepository.findByUserId(userId).orElse(null);
                if (cart == null || cart.getItems().isEmpty()) {
                    throw new IllegalArgumentException("Cart is empty");
                }
//
//            // Create the order
                Orders order = orderService.setNewOrderRazorpay(razorpayId, userId, selectedAddress, paymentMethod, cart);
//
//
//
//            // Reduce stock for each product in the cart
                for (Cart.CartItems item : cart.getItems()) {
                    String productId = item.getProductId();
                    int quantity = item.getQuantity();
                    Products product = productService.getProductById(productId);
                    if (product != null) {
                        int currentStock = product.getStockQuantityInt();
                        int newStock = currentStock - quantity;
                        product.setStock_quantity(String.valueOf(newStock));
                        productService.saveProduct(product);
                    }
                }
//
//
//
                if (!couponCode.isEmpty()) {
                    Coupon coupon = couponService.findByCouponCode(couponCode);
                    if (coupon != null) {
                        Set<Users> usedUsers = coupon.getUsedUsersIds();
                        usedUsers.add(currentUser);
                        coupon.setUsedUsersIds(usedUsers);
                        couponService.save(coupon);
                    }
                }
//
//
//
//            // Save offer information in order
                order.setOfferDiscount(cart.getOfferDiscountedValue());
                order.setCouponDiscount(cart.getCouponDiscountedValue());
                order.setAppliedOffers(cart.getAppliedOffers());

                if (order.getStatus().equals("FAILED_PAYMENT")) {
                    order.setStatus("PENDING");
                    orderRepository.save(order);
                }
//
//
//            // Clear the user's cart
                cart.getItems().clear();
                cart.setTotalPrice(0.0);
                cart.setCouponDiscountedValue(0.0);
                cart.setCouponCode(null);
                myCartRepository.save(cart);

                return ResponseEntity.ok().body(Collections.singletonMap("orderId", order.getId()));
            } catch (Exception e){
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
            }
        }

        // CheckoutController.java
        @GetMapping("checkout/payment-failed")
        public String handlePaymentFailure(

                Authentication authentication
        ) {
            Users user = authProviderFinder.getUserAuth(authentication);
            Cart existingCart = myCartRepository.findByUserId(user.getId()).orElse(null);
            Cart cart = myCartRepository.findById(existingCart.getId())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));



            // Create failed order without modifying cart
            Orders failedOrder = orderService.createFailedPaymentOrder(user.getId(), cart);

            return "redirect:/orders";
        }

        @PostMapping("/checkout/placeholder/wallet")
        public String placeOrderWithWallet(
                @RequestParam("selectedAddress") String selectedAddress,
                @RequestParam(name = "couponCode", required = false, defaultValue = "") String couponCode,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            Users currentUser = authProviderFinder.getUserAuth(authentication);
            String userId = userService.findByUsername(currentUser.getUsername()).getId();

            // Get user's wallet
            Wallet wallet = walletService.getWallet(userId);
            Cart cart = myCartRepository.findByUserId(userId).orElse(null);

            if (cart == null || cart.getItems().isEmpty()) {
                return "redirect:/cart";
            }

            double totalAmount = cart.getTotalPrice() - cart.getOfferDiscountedValue() - cart.getCouponDiscountedValue();

            // Check wallet balance
            if (wallet.getBalance() < totalAmount) {
                redirectAttributes.addFlashAttribute("error", "Insufficient wallet balance");
                return "redirect:/checkout";
            }



            // Create order
            Orders order = orderService.setNewOrder(userId, selectedAddress, PaymentType.WALLET, cart);

            // Deduct from wallet
            wallet.setBalance(wallet.getBalance() - totalAmount);
            walletService.saveWallet(wallet);

            // Record transaction
            TransactionHistory transaction = new TransactionHistory();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setAmount(String.valueOf(totalAmount));
            transaction.setStatus("success");
            transaction.setUserId(userId);
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setTransactionStatus(TransactionStatus.OUTGOING);
            transactionHistoryRepository.save(transaction);

            // Process coupon and clear cart (same as other methods)
            processOrderPostCreation(currentUser, cart, couponCode, order);

            return "redirect:/order-confirmation/" + order.getId();
        }




        // Extract common logic from original placeOrder method
        private void processOrderPostCreation(Users currentUser, Cart cart, String couponCode, Orders order) {
            // Reduce stock
            for (Cart.CartItems item : cart.getItems()) {
                String productId = item.getProductId();
                int quantity = item.getQuantity();
                Products product = productService.getProductById(productId);
                if (product != null) {
                    int currentStock = product.getStockQuantityInt();
                    int newStock = currentStock - quantity;
                    product.setStock_quantity(String.valueOf(newStock));
                    productService.saveProduct(product);
                }
            }

            // Handle coupon
            if (!couponCode.isEmpty()) {
                Coupon coupon = couponService.findByCouponCode(couponCode);
                if (coupon != null) {
                    Set<Users> usedUsers = coupon.getUsedUsersIds();
                    usedUsers.add(currentUser);
                    coupon.setUsedUsersIds(usedUsers);
                    couponService.save(coupon);
                }
            }

            // Clear cart
            cart.getItems().clear();
            cart.setTotalPrice(0.0);
            cart.setCouponDiscountedValue(0.0);
            cart.setCouponCode(null);
            myCartRepository.save(cart);
        }

        @GetMapping("/order-confirmation/{id}")
        public String confirmOrder(@PathVariable("id") String id, Model model) {
            model.addAttribute("orderId", id);
        System.out.println("INSIDE CONFIRMATION");

            System.out.println("orderId: " + id);

            Orders order = orderService.getOrderById(id);
    //        Orders order = orderService.getOrderById(id);
            if (order != null && order.getItems() != null) {
                // Create a map of productId -> Products object
                Map<String, Products> productsMap = new HashMap<>();
                for (Orders.OrderItem item : order.getItems()) {
                    Products product = productService.getProductById(item.getProductId());
                    if (product != null) {
                        productsMap.put(item.getProductId(), product);
                    }
                }
                System.out.println("Order address is "+ order.getShippingAddress());
                if (order.getShippingAddress() != null) {
                    System.out.println("The Address fetched from DB is " + order.getShippingAddress());
                    Address shippingAddress = addressService.getAddressById(order.getShippingAddress());
                    model.addAttribute("shippingAddress", shippingAddress);
                }
                model.addAttribute("order", order);
                model.addAttribute("productsMap", productsMap);
            }


            return "checkout/order-confirmation";
        }


        @PostMapping("/checkout/coupon-code")
        public String applyCoupon(@RequestParam("couponCode") String couponCode,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

            Users currentUser = authProviderFinder.getUserAuth(authentication);
            Users user = userService.findByUsername(currentUser.getUsername());
            Cart cart = myCartRepository.findByUserId(user.getId()).orElse(null);

            if (cart == null) {
                redirectAttributes.addFlashAttribute("couponCodeError", "Cart not found");
                return "redirect:/checkout";
            }

            Coupon coupon = couponRepository.findByCouponCode(couponCode).orElse(null);

            try {
                // Validate coupon exists
                if (coupon == null) {
                    throw new IllegalArgumentException("Invalid coupon code");
                }

                // Check if coupon is active
                if (!coupon.getisActive()) {
                    throw new IllegalArgumentException("Coupon is not active");
                }

                // Check expiration date
                if (coupon.getExpirationDate().before(new Date())) {
                    throw new IllegalArgumentException("Coupon has expired");
                }

                // Check minimum purchase amount
                if (coupon.getMinimumPurchaseAmount() != null &&
                        cart.getTotalPrice() < coupon.getMinimumPurchaseAmount().doubleValue()) {
                    throw new IllegalArgumentException("Minimum purchase amount not met");
                }

                // Check if user already used this coupon
                if (coupon.getUsedUsersIds().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
                    throw new IllegalArgumentException("Coupon already used");
                }

                // Calculate discount
                double discount = calculateDiscount(coupon, cart.getTotalPrice());

                // Apply to cart
                cart.setCouponCode(couponCode);
                cart.setCouponDiscountedValue(discount);
                myCartRepository.save(cart);

                redirectAttributes.addFlashAttribute("couponSuccess", "Coupon applied successfully!");
                redirectAttributes.addFlashAttribute("couponCode", couponCode);
                redirectAttributes.addFlashAttribute("couponDescription", coupon.getCouponDescription());

            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("couponCodeError", e.getMessage());
            }

            return "redirect:/checkout";
        }

        private double calculateDiscount(Coupon coupon, double cartTotal) {
            double discount = 0;

            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                discount = cartTotal * (coupon.getDiscountValue().doubleValue() / 100);

                // Apply maximum discount if specified
                if (coupon.getMaximumDiscountAmount() != null) {
                    discount = Math.min(discount, coupon.getMaximumDiscountAmount().doubleValue());
                }
            } else {
                discount = coupon.getDiscountValue().doubleValue();
            }

            return discount;
        }

        @Value("${razorpay.api.key}")
        private String razorpayKey;

        @Value("${razorpay.api.secret}")
        private String razorpaySecret;

        @PostMapping(value = "/create-razorpay-order", produces = "application/json")
        @ResponseBody
        public ResponseEntity<RazorDTO> createOrder(@RequestBody Map<String, Integer> orderRequest, Authentication authentication) throws RazorpayException {
            // in order request amount

            Users currentUser = authProviderFinder.getUserAuth(authentication);
            String username = currentUser.getUsername();
            String email = currentUser.getEmail();

        System.out.println("INSIDE Create Order");
        System.out.println("orderRequest: " + orderRequest);

            RazorDTO razorpayOrder = razorPayService.createOrder(orderRequest, username, email);
            System.out.println(razorpayOrder.toString());


            return new ResponseEntity<RazorDTO>(razorpayOrder, HttpStatus.OK);
        }




        @Autowired
        private TemplateEngine templateEngine;

        @GetMapping("/order/{id}/invoice")
        public ResponseEntity<byte[]> generateInvoice(@PathVariable String id) throws Exception {
            // Get order data
            Orders order = orderService.getOrderById(id);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Products> productsMap = new HashMap<>();
            for (Orders.OrderItem item : order.getItems()) {
                Products product = productService.getProductById(item.getProductId());
                if (product != null) {
                    productsMap.put(item.getProductId(), product);
                }
            }

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("productsMap", productsMap);

            // Process HTML template using autowired templateEngine
            String htmlContent = templateEngine.process("pdf/invoice", context);

            // Generate PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();

            byte[] pdfBytes = outputStream.toByteArray();

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }


    }
