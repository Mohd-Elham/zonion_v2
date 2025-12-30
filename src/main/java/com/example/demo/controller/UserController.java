package com.example.demo.controller;

import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.COUPON.DiscountType;
import com.example.demo.models.Category;
import com.example.demo.models.Products;
import com.example.demo.models.Users;
import com.example.demo.models.Wishlists;
import com.example.demo.models.offer.Offer;
import com.example.demo.models.offer.OfferType;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import com.example.demo.service.*;
import com.example.demo.service.profile.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {


    private final ProductService productService;
    private final UserService userService;
    private final AuthProviderFinder authProviderFinder;
    private final OfferService offerService;
    private final OfferCalculationService offerCalculationService;
    private final WishlistService wishlistService;
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    CategoryService categoryService;

    @Autowired
    public UserController(CategoryService categoryService, ProductService productService, UserService userService, AuthProviderFinder authProviderFinder, OfferService offerService, OfferCalculationService offerCalculationService, WishlistService wishlistService, WishlistRepository wishlistRepository, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.userService = userService;
        this.authProviderFinder = authProviderFinder;
        this.offerService = offerService;
        this.offerCalculationService = offerCalculationService;
        this.wishlistService = wishlistService;
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(ModelMap model, Authentication auth) {
        List<Category> categories = categoryService.getAllCategory();
        List<Products> products = productService.getAllProducts();

        // Detailed logging
        System.out.println("Number of categories: " + categories.size());
        for (Category category : categories) {
            System.out.println("Category Details:");
            System.out.println("ID: " + category.getId());
            System.out.println("Name: " + category.getCategory_name());
            System.out.println("Description: " + category.getCategory_description());
            System.out.println("Image Path: " + category.getImage_path());
        }

        model.addAttribute("categories", categories);
        model.addAttribute("topProducts", products);
        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();

        for (Products product : products) {
            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }
        }
        // Add to existing index method
        Users currentUser = authProviderFinder.getUserAuth(auth);
        if(currentUser != null && !currentUser.isReferralPromptShown() && !currentUser.isReferralCodeUsed()){
            model.addAttribute("showReferralModal", true);
//            currentUser.setReferralPromptShown(true);
            userRepository.save(currentUser);
        }



        List<String> wishlistProductIds = currentUser != null ?
                wishlistService.getWishlistProductIds(currentUser.getId()) :
                Collections.emptyList();

        model.addAttribute("wishlistProductIds", wishlistProductIds);

        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);
        return "dashboard";
    }

    private Offer findBestOffer(List<Offer> offers, double originalPrice) {
        Offer bestOffer = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (Offer offer : offers) {
            BigDecimal currentDiscount = offerCalculationService.calculateDiscount(
                    offer,
                    BigDecimal.valueOf(originalPrice)
            );

            if (currentDiscount.compareTo(maxDiscount) > 0) {
                maxDiscount = currentDiscount;
                bestOffer = offer;
            }
        }

        return bestOffer;
    }

    @GetMapping("/admin_login")
    public String admin(ModelMap model) {
        return "admin/admin_login";
    }






    @GetMapping("/duummy")
    public String duummy(ModelMap model) {
        return "admin/dummy";
    }

    @GetMapping("/product_page")
    public String productPage(ModelMap model) {
        return "main/products_detail_page";
    }
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable String id, ModelMap model, Authentication auth) {
        Products product = productService.getProductById(id);

        // Handle product not found
        if (product == null) {
            return "error/404";
        }
        Users currentUser = authProviderFinder.getUserAuth(auth);

        // Redirect if user not found
        if (currentUser == null) {
            return "redirect:/login";
        }

        String currentUserId = currentUser.getId();

        // Rest of your product detail logic...
        List<Products> relatedProducts = productService.getProductsByCategoryId(product.getCategory())
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4)
                .collect(Collectors.toList());

        // Collect product images
        List<String> images = new ArrayList<>();
        if (product.getImage_1() != null && !product.getImage_1().isEmpty()) images.add(product.getImage_1());
        if (product.getImage_2() != null && !product.getImage_2().isEmpty()) images.add(product.getImage_2());
        if (product.getImage_3() != null && !product.getImage_3().isEmpty()) images.add(product.getImage_3());
        if (product.getImage_4() != null && !product.getImage_4().isEmpty()) images.add(product.getImage_4());

        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();


            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }

        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);

        // Add attributes to model
        model.addAttribute("product", product);
        model.addAttribute("images", images);
        model.addAttribute("userId", currentUser);
        model.addAttribute("relatedProducts", relatedProducts);

        return "main/products_detail_page";
    }

//    @GetMapping("/category/{id}")
//    public String categoryPage(@PathVariable String id, ModelMap model, @RequestParam(value = "sortOrder", defaultValue = "default") String sortOrder) {
//        List<Category> categories = categoryService.getAllCategory();
//        List<Products> categoryProducts = productService.getProductsByCategoryId(id);
//        Category currentCategory = categoryService.getCategoryById(id);
//        List<Products> sortedProducts;
//
//        switch (sortOrder) {
//            case "price_asc":
//                sortedProducts = categoryProducts.stream()
//                        .sorted(Comparator.comparing(Products::getPrice))
//                        .collect(Collectors.toList());
//                break;
//            case "price_desc":
//                sortedProducts = categoryProducts.stream()
//                        .sorted(Comparator.comparing(Products::getPrice).reversed())
//                        .collect(Collectors.toList());
//                break;
//            default:
//                sortedProducts = categoryProducts; // Default sorting or no sorting
//                break;
//        }
//
//        Map<String, Double> discountedPrices = new HashMap<>();
//        Map<String, String> discountTexts = new HashMap<>();
//        List<Offer> activeOffers = offerService.findActiveOffers();
//
//        for (Products product : sortedProducts) {
//            try {
//                double originalPrice = Double.parseDouble(product.getPrice());
//                List<Offer> productOffers = new ArrayList<>();
//                List<Offer> categoryOffers = new ArrayList<>();
//
//                // Separate offers into product-specific and category-based
//                for (Offer offer : activeOffers) {
//                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
//                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));
//
//                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
//                            product.getCategory() != null &&
//                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));
//
//                    if (isProductOffer) {
//                        productOffers.add(offer);
//                    } else if (isCategoryOffer) {
//                        categoryOffers.add(offer);
//                    }
//                }
//
//                // Find best offers for each type
//                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
//                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);
//
//                // Prioritize product offers over category offers
//                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;
//
//                if (bestOffer != null) {
//                    BigDecimal discount = offerCalculationService.calculateDiscount(
//                            bestOffer,
//                            BigDecimal.valueOf(originalPrice)
//                    );
//
//                    double discountedPrice = originalPrice - discount.doubleValue();
//                    discountedPrices.put(product.getId(), discountedPrice);
//
//                    String discountText = "";
//                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
//                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
//                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
//                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
//                    }
//
//                    discountTexts.put(product.getId(), discountText);
//                }
//            }  catch (NumberFormatException e) {
//                // Handle invalid price format
//            }
//        }
//        model.addAttribute("discountedPrices", discountedPrices);
//        model.addAttribute("discountTexts", discountTexts);
//
//
//        model.addAttribute("categories", categories);
//        model.addAttribute("products", sortedProducts); // Use sorted products here
//        model.addAttribute("currentCategory", currentCategory);
//        model.addAttribute("sortOrder", sortOrder); // Pass sortOrder to the template
//        return "main/category_page";
//    }


    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, ModelMap model) {
        List<Products> searchResults = productService.searchProducts(query);
        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();

        for (Products product : searchResults) {
            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }
        }
        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);
        model.addAttribute("products", searchResults);
        model.addAttribute("searchQuery", query);
        return "main/search"; // This should match your search results template name
    }

    @GetMapping("/shop")
    public String shopPage(ModelMap model,
                           @RequestParam(value = "sortOrder", defaultValue = "default") String sortOrder,
                           @RequestParam(value = "page", defaultValue = "1") int page
    ) {

        int size = 6;
        Sort sort = determineSort(sortOrder);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Products> productsPage = productService.getAllProductsByPages(pageable);


        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();


//        List<Products> allProducts = productService.getAllProducts();
//
//        List<Products> sortedProducts = switch (sortOrder) {
//            case "price_asc" -> allProducts.stream()
//                    .sorted(Comparator.comparing(Products::getPrice))
//                    .collect(Collectors.toList());
//            case "price_desc" -> allProducts.stream()
//                    .sorted(Comparator.comparing(Products::getPrice).reversed())
//                    .collect(Collectors.toList());
//            case "name_asc" -> allProducts.stream()
//                    .sorted(Comparator.comparing(Products::getProduct_name))
//                    .collect(Collectors.toList());
//            case "name_desc" -> allProducts.stream()
//                    .sorted(Comparator.comparing(Products::getProduct_name).reversed())
//                    .collect(Collectors.toList());
//            default -> allProducts;
//        };
//
//        Map<String, Double> discountedPrices = new HashMap<>();
//        Map<String, String> discountTexts = new HashMap<>();
//        List<Offer> activeOffers = offerService.findActiveOffers();

        for (Products product : productsPage.getContent()) {
            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }
        }
        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());

        return "main/shop";
    }

    private Sort determineSort(String sortOrder) {
        switch (sortOrder) {
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "price");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "price");
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "product_name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "product_name");
            default: return Sort.unsorted();
        }
    }


    @PostMapping("/wishlist/toggle/{productId}")
    public String toggleWishlist(@PathVariable String productId, Authentication auth) {
        Users user = authProviderFinder.getUserAuth(auth);
        if (user == null) return "redirect:/login";

        wishlistService.toggleProductInWishlist(user.getId(), productId);
        return "redirect:/";
    }

    @GetMapping("/wishlist")
    public String wishlistPage(ModelMap model, Authentication auth) {
        Users user = authProviderFinder.getUserAuth(auth);
        if (user == null) return "redirect:/login";

        Wishlists wishlist = wishlistService.getWishlistByUserId(user.getId());
        List<Products> wishlistProducts = new ArrayList<>();

        if (wishlist != null) {
            wishlistProducts = wishlist.getProductId().stream()
                    .map(productService::getProductById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        List<Category> categories = categoryService.getAllCategory();
        List<Products> products = productService.getAllProducts();


        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();

        for (Products product : products) {
            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }
        }

        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);


        // Add discount calculations similar to other pages
        model.addAttribute("wishlistProducts", wishlistProducts);
        return "main/wishlist";
    }

    @PostMapping("wishlist/remove-all")
    public String removeAllFromWishlist(Authentication auth) {
        Users user = authProviderFinder.getUserAuth(auth);

        Wishlists wishlist = wishlistService.getWishlistByUserId(user.getId());
        wishlist.setProductId(new ArrayList<>());
        wishlistRepository.save(wishlist);


        return "redirect:/wishlist";
    }

    @GetMapping("/category/{id}")
    public String categoryPage(@PathVariable String id,
                               @RequestParam(value = "sortOrder", defaultValue = "default") String sortOrder,
                               @RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               ModelMap model) {
        int size = 6;
        Sort sort = determineSort(sortOrder);
        Pageable pageable = PageRequest.of(page-1, size, sort);


        Page<Products> productPage;
        if (query != null && !query.isEmpty()) {
            // Implement search with pagination if needed
            productPage = productService.searchProductsByCategory(query, id, pageable);
        } else {
            productPage = productService.getProductsByCategoryId(id, pageable);
        }

        List<Category> categories = categoryService.getAllCategory();
        List<Products> categoryProducts = productService.getProductsByCategoryId(id);
        Category currentCategory = categoryService.getCategoryById(id);

        Map<String, Double> discountedPrices = new HashMap<>();
        Map<String, String> discountTexts = new HashMap<>();
        List<Offer> activeOffers = offerService.findActiveOffers();


        for (Products product : productPage.getContent()) {
            try {
                double originalPrice = Double.parseDouble(product.getPrice());
                List<Offer> productOffers = new ArrayList<>();
                List<Offer> categoryOffers = new ArrayList<>();

                // Separate offers into product-specific and category-based
                for (Offer offer : activeOffers) {
                    boolean isProductOffer = offer.getOfferType() == OfferType.PRODUCT_OFFER &&
                            offer.getProducts().stream().anyMatch(p -> p.getId().equals(product.getId()));

                    boolean isCategoryOffer = offer.getOfferType() == OfferType.CATEGORY_OFFER &&
                            product.getCategory() != null &&
                            offer.getCategories().stream().anyMatch(c -> c.getId().equals(product.getCategory()));

                    if (isProductOffer) {
                        productOffers.add(offer);
                    } else if (isCategoryOffer) {
                        categoryOffers.add(offer);
                    }
                }

                // Find best offers for each type
                Offer bestProductOffer = findBestOffer(productOffers, originalPrice);
                Offer bestCategoryOffer = findBestOffer(categoryOffers, originalPrice);

                // Prioritize product offers over category offers
                Offer bestOffer = bestProductOffer != null ? bestProductOffer : bestCategoryOffer;

                if (bestOffer != null) {
                    BigDecimal discount = offerCalculationService.calculateDiscount(
                            bestOffer,
                            BigDecimal.valueOf(originalPrice)
                    );

                    double discountedPrice = originalPrice - discount.doubleValue();
                    discountedPrices.put(product.getId(), discountedPrice);

                    String discountText = "";
                    if (bestOffer.getDiscountType() == DiscountType.PERCENTAGE){
                        discountText = bestOffer.getDiscountValue().intValue() + "% OFF" ;
                    } else if (bestOffer.getDiscountType() == DiscountType.FLAT) {
                        discountText = "₹" + bestOffer.getDiscountValue() + " OFF";
                    }

                    discountTexts.put(product.getId(), discountText);
                }
            }  catch (NumberFormatException e) {
                // Handle invalid price format
            }
        }


        // Apply search filter if query exists
        if (query != null && !query.isEmpty()) {
            categoryProducts = categoryProducts.stream()
                    .filter(p -> p.getProduct_name().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Sorting logic remains the same
        List<Products> sortedProducts = applySorting(categoryProducts, sortOrder);

        // ... existing discount calculation logic ...

        model.addAttribute("categories", categoryService.getAllCategory());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentCategory", categoryService.getCategoryById(id));
        model.addAttribute("discountedPrices", discountedPrices);
        model.addAttribute("discountTexts", discountTexts);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("searchQuery", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "main/category_page";
    }

    private List<Products> applySorting(List<Products> products, String sortOrder) {
        switch (sortOrder) {
            case "price_asc":
                return products.stream()
                        .sorted(Comparator.comparing(Products::getPrice))
                        .collect(Collectors.toList());
            case "price_desc":
                return products.stream()
                        .sorted(Comparator.comparing(Products::getPrice).reversed())
                        .collect(Collectors.toList());
            case "name_asc":
                return products.stream()
                        .sorted(Comparator.comparing(Products::getProduct_name))
                        .collect(Collectors.toList());
            case "name_desc":
                return products.stream()
                        .sorted(Comparator.comparing(Products::getProduct_name).reversed())
                        .collect(Collectors.toList());
            default:
                return products;
        }
    }
}
