    package com.example.demo.controller;

    import com.example.demo.models.COUPON.Coupon;
    import com.example.demo.models.COUPON.DiscountType;
    import com.example.demo.service.CouponService;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.ModelMap;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.math.BigDecimal;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.List;
    import java.util.UUID;

    @Controller
    public class CouponController {

        private CouponService couponService;

        public CouponController(CouponService couponService) {
            this.couponService = couponService;
        }

        @GetMapping("/admin_dashboard/coupons")
        public String coupons(ModelMap model) {
            List<Coupon> coupons = couponService.findAll();
            model.addAttribute("coupons", coupons);
            return "admin/coupon/coupons";
        }

        @GetMapping("/admin_dashboard/add_coupon")
        public String addCoupon(ModelMap model) {

            return "admin/coupon/add_coupon";
        }

        @PostMapping("/coupons/add")
        public String saveCoupon(@RequestParam("couponCode") String couponCode,
                                 @RequestParam("couponName") String couponName,
                                 @RequestParam("couponDescription") String couponDescription,
                                 @RequestParam("discountType") String discountType,
                                 @RequestParam("discountValue") BigDecimal discountValue,
                                 @RequestParam("expirationDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expirationDate,
                                 @RequestParam("minimumPurchaseAmount") BigDecimal minimumPurchaseAmount,
                                 @RequestParam("maximumDiscountAmount") BigDecimal maximumDiscountAmount,
                                 RedirectAttributes redirectAttributes) {

                System.out.println("INSIDE ADD COUPON");
                Coupon coupon = new Coupon();
                String randUUid = UUID.randomUUID().toString();
                coupon.setId(randUUid);
                coupon.setCouponCode(couponCode);
                coupon.setCouponName(couponName);
                coupon.setCouponDescription(couponDescription);

                // Convert string enum to enum type
                coupon.setDiscountType(DiscountType.valueOf(discountType));
                coupon.setDiscountValue(discountValue);

                // Parse date from string
    //            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                coupon.setExpirationDate(expirationDate);

                // Set creation date to now
                coupon.setCreatedAt(new Date());

                coupon.setMinimumPurchaseAmount(minimumPurchaseAmount);
                coupon.setMaximumDiscountAmount(maximumDiscountAmount);

                coupon.setIsActive(true);

                System.out.println("Coupon code: " + coupon.toString());

                // Save to database
                couponService.save(coupon);

                redirectAttributes.addFlashAttribute("successMessage", "Coupon created successfully!");
                return "redirect:/admin_dashboard/coupons";

        }

        @GetMapping("/admin_dashboard/coupons/edit/{id}")
        public String showEditForm(@PathVariable String id, ModelMap model) {
            Coupon coupon = couponService.findById(id);
            List<DiscountType> discountTypes = List.of(DiscountType.values());
            model.addAttribute("coupon", coupon);
            model.addAttribute("discountTypes", discountTypes);

            return "admin/coupon/edit_coupon";
        }

        @PostMapping("/coupons/update")
        public String updateCoupon(@RequestParam("id") String id,
                                   @RequestParam("couponCode") String couponCode,
                                   @RequestParam("couponName") String couponName,
                                   @RequestParam("couponDescription") String couponDescription,
                                   @RequestParam("discountType") String discountType,
                                   @RequestParam("discountValue") BigDecimal discountValue,
                                   @RequestParam("expirationDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date expirationDate,
                                   @RequestParam("minimumPurchaseAmount") BigDecimal minimumPurchaseAmount,
                                   @RequestParam("maximumDiscountAmount") BigDecimal maximumDiscountAmount,
                                   RedirectAttributes redirectAttributes) {

            Coupon coupon = couponService.findById(id);

            coupon.setCouponCode(couponCode);
            coupon.setCouponName(couponName);
            coupon.setCouponDescription(couponDescription);
            coupon.setDiscountType(DiscountType.valueOf(discountType));
            coupon.setDiscountValue(discountValue);
            coupon.setExpirationDate(expirationDate);
            coupon.setMinimumPurchaseAmount(minimumPurchaseAmount);
            coupon.setMaximumDiscountAmount(maximumDiscountAmount);

            couponService.save(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon updated successfully!");
            return "redirect:/admin_dashboard/coupons";
        }

        @PostMapping("coupons/toggle/{id}")
        public String toggleCoupon(@PathVariable String id, ModelMap model) {

            Coupon coupon = couponService.findById(id);
            couponService.toggleCoupon(coupon);

            return "redirect:/admin_dashboard/coupons";
        }

    }
