package com.example.demo.controller;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.models.Category;
import com.example.demo.models.Products;
import com.example.demo.service.CategoryService;
import com.example.demo.service.profile.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class ProductController {



    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/images/uploads";
    private final CategoryService categoryService;
    private final ProductService productService;
    private final Cloudinary cloudinary;

    public ProductController(ProductService productService, CategoryService categoryService, Cloudinary cloudinary) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.cloudinary = cloudinary;
    }



    @GetMapping("/admin_dashboard/products")
    public String products(ModelMap model) {
        List<Products> productsList = productService.getAllProducts();
        List<Category> categories = categoryService.getAllCategory();


        Map<String, String> categoryMap = new HashMap<>();

        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getCategory_name());
        }

        model.addAttribute("products", productsList);
        model.addAttribute("categoryMap", categoryMap); // Add the map to the model
        return "admin/product/products";
    }


    @GetMapping("/admin_dashboard/add_products")
    public String addProducts(ModelMap model) {

        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categoriess", categories);

        return "admin/product/add_product";
    }
    @GetMapping("/admin_dashboard/products/edit/{id}")
    public String showEditForm(@PathVariable String id, ModelMap model) {
        Products product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/product/edit_product";
    }

    @PostMapping("/products/add")
    public String addProduct(
            @RequestParam("category") String categoryId,
            @RequestParam("product_name") String productName,
            @RequestParam("description") String description,
            @RequestParam("stock_quantity") String stockQuantity,
            @RequestParam("price") String price,
            @RequestParam(value = "croppedImage1", required = false) String croppedImage1,
            @RequestParam(value = "croppedImage2", required = false) String croppedImage2,
            @RequestParam(value = "croppedImage3", required = false) String croppedImage3,
            @RequestParam(value = "croppedImage4", required = false) String croppedImage4
    ) {
        String savedPath1 = null;
        String savedPath2 = null;
        String savedPath3 = null;
        String savedPath4 = null;
        System.out.println("categoryId: " + categoryId + "productName: " + productName + "description: " + description
                + "stockQuantity: " + stockQuantity + "price: " + price + "cropped image data " + croppedImage1 +
                "cropped image 2 " + croppedImage2 + "cropped image 3 " + croppedImage3 + "cropped image 4 " + croppedImage4);

        Products exisitngProduct = productService.getPrductsByProductName(productName);
        if (exisitngProduct != null) {
            return "redirect:/admin_dashboard/add_products?error=Product+"+productName+"+already+exists";
        }
        try{
//            if (croppedImage1 != null && croppedImage2 != null && croppedImage3 != null && croppedImage4 != null) {
//                String base64Image1 = croppedImage1.split(",")[1];
//                String base64Image2 = croppedImage2.split(",")[1];
//                String base64Image3 = croppedImage3.split(",")[1];
//                String base64Image4 = croppedImage4.split(",")[1];
//
//                String fileName1 = UUID.randomUUID().toString() + ".png";
//                String fileName2 = UUID.randomUUID().toString() + ".png";
//                String fileName3 = UUID.randomUUID().toString() + ".png";
//                String fileName4 = UUID.randomUUID().toString() + ".png";
//
//                Path path1 = Paths.get(IMAGE_UPLOAD_DIR, fileName1);
//                Files.write(path1, Base64.getDecoder().decode(base64Image1));
//
//                Path path2 = Paths.get(IMAGE_UPLOAD_DIR, fileName2);
//                Files.write(path2, Base64.getDecoder().decode(base64Image2));
//
//                Path path3 = Paths.get(IMAGE_UPLOAD_DIR, fileName3);
//                Files.write(path3, Base64.getDecoder().decode(base64Image3));
//
//                Path path4 = Paths.get(IMAGE_UPLOAD_DIR, fileName4);
//                Files.write(path4, Base64.getDecoder().decode(base64Image4));
//
//                savedPath1 = "/images/uploads/" + fileName1;
//                savedPath2 = "/images/uploads/" + fileName2;
//                savedPath3 = "/images/uploads/" + fileName3;
//                savedPath4 = "/images/uploads/" + fileName4;
//
//                System.out.println("savedPath1: " + savedPath1 + ",savedPath2: " + savedPath2 + ",savedPath3: " + savedPath3);
//
//                productService.addProduct(categoryId, productName, description, stockQuantity, price, savedPath1, savedPath2, savedPath3, savedPath4);

            Products existingProduct = productService.getPrductsByProductName(productName);
            if (existingProduct != null) {
                return "redirect:/admin_dashboard/add_products?error=Product+"+productName+"+already+exists";
            }

            String imageUrl1 = null;
            String imageUrl2 = null;
            String imageUrl3 = null;
            String imageUrl4 = null;

            // Upload images to Cloudinary
            if (croppedImage1 != null && !croppedImage1.isEmpty()) {
                imageUrl1 = uploadToCloudinary(croppedImage1);
            }
            if (croppedImage2 != null && !croppedImage2.isEmpty()) {
                imageUrl2 = uploadToCloudinary(croppedImage2);
            }
            if (croppedImage3 != null && !croppedImage3.isEmpty()) {
                imageUrl3 = uploadToCloudinary(croppedImage3);
            }
            if (croppedImage4 != null && !croppedImage4.isEmpty()) {
                imageUrl4 = uploadToCloudinary(croppedImage4);
            }

            productService.addProduct(categoryId, productName, description, stockQuantity, price,
                    imageUrl1, imageUrl2, imageUrl3, imageUrl4);
            return "redirect:/admin_dashboard/products";

        } catch (Exception e){
            e.printStackTrace();

            return "error-page";
        }


//        return "redirect:/admin_dashboard/products";


    }

    private String uploadToCloudinary(String base64Image) throws Exception {
        String base64Data = base64Image.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        Map<String, Object> uploadResult = cloudinary.uploader()
                .upload(imageBytes, ObjectUtils.asMap(
                        "folder", "products",
                        "public_id", UUID.randomUUID().toString()
                ));

        return (String) uploadResult.get("secure_url");
    }

    @PostMapping("/products/delete")
    public String deleteProduct(@RequestParam("id") String id) {
        productService.deleteProduct(id);
        return "redirect:/admin_dashboard/products";
    }


    @PostMapping("/products/update")
    public String updateProduct(
            @RequestParam("product_id") String productId,
            @RequestParam("category") String categoryId,
            @RequestParam("product_name") String productName,
            @RequestParam("description") String description,
            @RequestParam("stock_quantity") String stockQuantity,
            @RequestParam("price") String price,
            @RequestParam(value = "croppedImage1", required = false) String croppedImage1,
            @RequestParam(value = "croppedImage2", required = false) String croppedImage2,
            @RequestParam(value = "croppedImage3", required = false) String croppedImage3,
            @RequestParam(value = "croppedImage4", required = false) String croppedImage4) {

//        System.out.println("Received request to update product with ID: " + productId);
//
//        Products existingProduct = productService.getProductById(productId);
//
//
//        System.out.println("Existing product details before update:");
//        System.out.println("Category: " + existingProduct.getCategory());
//        System.out.println("Name: " + existingProduct.getProduct_name());
//        System.out.println("Description: " + existingProduct.getProduct_description());
//        System.out.println("Stock Quantity: " + existingProduct.getStock_quantity());
//        System.out.println("Price: " + existingProduct.getPrice());
//
//        Products exisitngProduct = productService.getPrductsByProductName(productName);
//        if (exisitngProduct != null) {
//            return "redirect:/admin_dashboard/products/edit/"+productId+"?error=Product+"+productName+"+already+exists";
//        }
//        if(croppedImage1.length() > 10){
//
//            System.out.println("croppedImage1: is retirved "  );
//        } else {
//            System.out.println("croppedImage1 is not retrived");
//        }
//        if(croppedImage2.length() > 10){
//            System.out.println("croppedImage2: is retirved "  );
//        } else {
//            System.out.println("croppedImage2 is not retirved");
//        }
//        if(croppedImage3.length() > 10){
//            System.out.println("croppedImage3: is retirved "  );
//        } else {
//            System.out.println("croppedImage3 is not retirved");
//        }
//        if(croppedImage4.length() > 10){
//            System.out.println("croppedImage4: is retirved " );
//        } else {
//            System.out.println("croppedImage4 is not retirved");
//        }
//
//        System.out.println("Existing Image Paths: " +
//                existingProduct.getImage_1() + ", " +
//                existingProduct.getImage_2() + ", " +
//                existingProduct.getImage_3() + ", " +
//                existingProduct.getImage_4());
//
//        String imagePath1 = existingProduct.getImage_1();
//        String imagePath2 = existingProduct.getImage_2();
//        String imagePath3 = existingProduct.getImage_3();
//        String imagePath4 = existingProduct.getImage_4();

        try {
            // Process each image individually
            Products existingProduct = productService.getProductById(productId);
            Products existingWithName = productService.getPrductsByProductName(productName);

            if (existingWithName != null && !existingWithName.getId().equals(productId)) {
                return "redirect:/admin_dashboard/products/edit/"+productId+"?error=Product+"+productName+"+already+exists";
            }

            // Get existing image URLs
            String imageUrl1 = existingProduct.getImage_1();
            String imageUrl2 = existingProduct.getImage_2();
            String imageUrl3 = existingProduct.getImage_3();
            String imageUrl4 = existingProduct.getImage_4();

            // Update images only if new ones are provided
            if (croppedImage1 != null && !croppedImage1.isEmpty()) {
                imageUrl1 = uploadToCloudinary(croppedImage1);
            }
            if (croppedImage2 != null && !croppedImage2.isEmpty()) {
                imageUrl2 = uploadToCloudinary(croppedImage2);
            }
            if (croppedImage3 != null && !croppedImage3.isEmpty()) {
                imageUrl3 = uploadToCloudinary(croppedImage3);
            }
            if (croppedImage4 != null && !croppedImage4.isEmpty()) {
                imageUrl4 = uploadToCloudinary(croppedImage4);
            }

            productService.updateProducts(productId, categoryId, productName, description,
                    stockQuantity, price, imageUrl1, imageUrl2, imageUrl3, imageUrl4);
            return "redirect:/admin_dashboard/products";

        } catch (Exception e) {
            System.out.println("Error occurred while updating product: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin_dashboard/products?error=UpdateFailed";
        }

//        return "redirect:/admin_dashboard/products";
    }



}
