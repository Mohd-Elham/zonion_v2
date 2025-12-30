package com.example.demo.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.models.Category;
import com.example.demo.service.CategoryService;
import com.example.demo.service.profile.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class CategoryController {

    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/images/uploads";
    private final Cloudinary cloudinary;
    private CategoryService categoryService;

    private ProductService productService;

    @Autowired
    public CategoryController(CategoryService categoryService, ProductService productService, Cloudinary cloudinary) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.cloudinary = cloudinary;
    }

    @GetMapping("/admin_dashboard/add_category")
    public String addCategory(ModelMap model) {
        return "admin/category/add_category";
    }

    @PostMapping("/categories/add")
    public String addCategory(
            @RequestParam("categoryName") String name,
            @RequestParam("description") String description,
            @RequestParam("croppedImage") String croppedImage,
            Model model
    ) {
        String savedImagePath = null;
        System.out.println("name: " + name + ", description: " + description + ", croppedImage: " + croppedImage);
        try {

            Category ExsitingCateogryName = categoryService.getCategoryByName(name);
            if (ExsitingCateogryName != null) {
                return "redirect:/admin_dashboard/add_category?error1=Category+"+name+"+already+exists";
            }
            // Ensure the upload directory exists
//            File uploadDir = new File(IMAGE_UPLOAD_DIR);
//            if (!uploadDir.exists()) {
//                uploadDir.mkdirs();
//            }
//
//            // Process the cropped image
//            if (croppedImage != null && !croppedImage.isEmpty()) {
//                String base64Image = croppedImage.split(",")[1];
//                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
//
//                String fileName = UUID.randomUUID() + ".png";
//                Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
//                Files.write(filePath, imageBytes);
//
//                // Save the relative path for database storage
//                savedImagePath = "/images/uploads/" + fileName;
//                System.out.println("Cropped image saved to: " + filePath);
//            }
//
//            // Save the category to the database
//            categoryService.saveToCategory(name, description, savedImagePath);


            String imageUrl = null;
            if (croppedImage != null && !croppedImage.isEmpty()) {
                String base64EncodedImage = croppedImage.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64EncodedImage);

                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        imageBytes, ObjectUtils.asMap(
                                "folder", "categories",
                                "public_id", UUID.randomUUID().toString()
                        )
                );
                imageUrl = uploadResult.get("secure_url").toString();
                System.out.println("XXXXXXXXXXimageUrl: " + imageUrl);
            }
            categoryService.saveToCategory(name, description, imageUrl);
            System.out.println("Category saved successfully!" + name + " " + description + " " + savedImagePath);

        } catch (Exception e) {
            e.printStackTrace();
            return "error-page"; // Redirect to an error page in case of an exception
        }

        // Redirect to the add category page
        return "redirect:/admin_dashboard/category";
    }


//    main page
    @GetMapping("/admin_dashboard/category")
    public String category(ModelMap model) {
        List<Category> categories = categoryService.getAllCategory();

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
        return "admin/category/category";
    }


    //deleting
    @PostMapping("/categories/delete")
    public String deleteCategory(@RequestParam("id") String id) {
        categoryService.deleteCategory(id);
        productService.deleteProductByCateogoryId(id);
        return "redirect:/admin_dashboard/category";
    }

//    edit category
    @GetMapping("/categories/edit/{id}")
    public String shoeEditPage(ModelMap model, @PathVariable("id") String id) {
        Category category = categoryService.getCategoryByid(id);

        model.addAttribute("category", category);
        return "admin/category/edit-category";
    }

    @PostMapping("/categories/update")
    public String updateCategory(
            @RequestParam("id") String id,
            @RequestParam("categoryName") String name,
            @RequestParam("description") String description,
            @RequestParam(value = "croppedImage", required = false) String croppedImage) { // Add required=false

        Category existingCategory = categoryService.getCategoryByid(id);
        Category ExsitingCateogryName = categoryService.getCategoryByName(name);
        if (ExsitingCateogryName != null) {
            return "redirect:/categories/edit/"+id+"?error1=Category+"+name+"+already+exists";
        }

        try {
//            if (croppedImage != null && !croppedImage.isEmpty()) {
//                // Process new image
//                String base64Image = croppedImage.split(",")[1];
//                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
//
//                String fileName = UUID.randomUUID() + ".png";
//                Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
//                Files.write(filePath, imageBytes);
//
//                imagePath = "/images/uploads/" + fileName; // Update image path
//            }
            String imageUrl = existingCategory.getImage_path();
            if (croppedImage != null && !croppedImage.isEmpty()) {
                String base64Image = croppedImage.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                Map<String, Object> uploadResult = cloudinary.uploader()
                        .upload(imageBytes, ObjectUtils.asMap(
                                "folder", "categories",
                                "public_id", UUID.randomUUID().toString()
                        ));

                imageUrl = (String) uploadResult.get("secure_url");
            }


            // Update existing category
            categoryService.updateCategory(id, name, description, imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin_dashboard/category?error";
        }

        return "redirect:/admin_dashboard/category";
    }
}

