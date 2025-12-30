package com.example.demo.controller.admin.offer;

import ch.qos.logback.core.model.Model;
import com.example.demo.models.COUPON.DiscountType;
import com.example.demo.models.Category;
import com.example.demo.models.Products;
import com.example.demo.models.offer.Offer;
import com.example.demo.models.offer.OfferType;
import com.example.demo.service.CategoryService;
import com.example.demo.service.OfferService;
import com.example.demo.service.profile.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
public class OfferController {

    private final OfferService offerService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public OfferController(OfferService offerService, ProductService productService, CategoryService categoryService) {
        this.offerService = offerService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin_dashboard/offers")
    public String offers(ModelMap model) {

        List<Offer> offers = offerService.findAll();

        model.addAttribute("offers", offers);

        return "admin/offers";
    }

    @PostMapping("/admin_dashboard/offers/add")
    public String handleAddOffer(
            @RequestParam String offerName,
            @RequestParam String offerDescription,
            @RequestParam OfferType offerType,
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) List<String> categories,
            @RequestParam DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minimumDiscountAmount,
            @RequestParam(required = false) BigDecimal maximumDiscountAmount,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        Set<Products> productSet = productService.getSetProductsByIds(products);
        Set<Category> categorySet = categoryService.getCategoriesByIds(categories);

        Offer newOffer = new Offer(
                UUID.randomUUID().toString(),
                offerName,
                offerDescription,
                offerType,
                productSet,
                categorySet,
                discountType,
                discountValue,
                minimumDiscountAmount,
                maximumDiscountAmount,
                startDate,
                endDate,
                discountValue, // Simplified for example
                true
        );

        offerService.saveOffer(newOffer);
        return "redirect:/admin_dashboard/offers";
    }

    @GetMapping("/admin_dashboard/add_offer")
    public String addOffer(ModelMap model) {

        List<Products> products = productService.findAll();
        List<Category> categories = categoryService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
//        model.addAttribute("offerTypes", OfferType.values());
//        model.addAttribute("discountTypes", DiscountType.values());


        return "admin/offer/add_offer";
    }

    @GetMapping("/admin_dashboard/offer/edit/{id}")
    public String editOffer(@PathVariable String id, ModelMap model) {
        Offer offer = offerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid offer Id:" + id));

        List<Products> products = productService.findAll();
        List<Category> categories = categoryService.findAll();

        DiscountType discountType = offer.getDiscountType();

        model.addAttribute("offer", offer);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
//        model.addAttribute("discountType", discountType);

        return "admin/offer/edit_offer";
    }

    @PostMapping("/admin_dashboard/offers/update/{id}")
    public String updateOffer(
            @PathVariable String id,
            @RequestParam String offerName,
            @RequestParam String offerDescription,
            @RequestParam OfferType offerType,
            @RequestParam(required = false) List<String> products,
            @RequestParam(required = false) List<String> categories,
            @RequestParam DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minimumDiscountAmount,
            @RequestParam(required = false) BigDecimal maximumDiscountAmount,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        Offer existingOffer = offerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid offer Id:" + id));

        Set<Products> productSet = productService.getSetProductsByIds(products);
        Set<Category> categorySet = categoryService.getCategoriesByIds(categories);

        existingOffer.setOfferName(offerName);
        existingOffer.setOfferDescription(offerDescription);
        existingOffer.setOfferType(offerType);
        existingOffer.setProducts(productSet);
        existingOffer.setCategories(categorySet);
        existingOffer.setDiscountType(discountType);
        existingOffer.setDiscountValue(discountValue);
        existingOffer.setMinimumDiscountAmount(minimumDiscountAmount);
        existingOffer.setMaximumDiscountAmount(maximumDiscountAmount);
        existingOffer.setStartDate(startDate);
        existingOffer.setEndDate(endDate);

        offerService.saveOffer(existingOffer);
        return "redirect:/admin_dashboard/offers";
    }



}
