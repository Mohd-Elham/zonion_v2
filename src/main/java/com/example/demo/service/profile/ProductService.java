package com.example.demo.service.profile;

import com.example.demo.models.Products;
import com.example.demo.repository.ProductsRepository;
import com.example.demo.service.InventoryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService {

    private InventoryHistoryService inventoryHistoryService;
    private ProductsRepository productsRepository;

    @Autowired
    public ProductService(ProductsRepository productsRepository, InventoryHistoryService inventoryHistoryService) {
        this.productsRepository = productsRepository;
        this.inventoryHistoryService = inventoryHistoryService;
    }

    public void  addProduct(String category, String productName, String description, String stockQuantity, String price, String image1, String image2, String image3, String image4) {

        String randId = UUID.randomUUID().toString();
        Products newProduct = new Products(randId,category,productName,description,stockQuantity,price,image1,image2,image3,image4);
        productsRepository.save(newProduct);


    }

    public List<Products> getAllProducts() {

        return productsRepository.findAll();
    }

    public Products getProductById(String productId) {
        Optional<Products> products = productsRepository.findById(productId);

        if (products.isPresent()) {
            return products.get();
        }
        return null;
    }

    public void updateProducts(String productId, String categoryId, String productName, String description,
                               String stockQuantity, String price, String imagePath1, String imagePath2,
                               String imagePath3, String imagePath4) {
       Products newProduct = getProductById(productId);
       newProduct.setCategory(categoryId);
       newProduct.setProduct_name(productName);
       newProduct.setProduct_description(description);
       newProduct.setStock_quantity(stockQuantity);
       newProduct.setPrice(price);
       if (imagePath1 != null && imagePath2 != null && imagePath3 != null && imagePath4 != null) {
           newProduct.setImage_1(imagePath1);
           newProduct.setImage_2(imagePath2);
           newProduct.setImage_3(imagePath3);
           newProduct.setImage_4(imagePath4);
       }
       productsRepository.save(newProduct);

    }

    public void deleteProduct(String id) {
        productsRepository.deleteById(id);
    }

    public List<Products> getProductsByCategoryId(String id) {
        return productsRepository.findByCategory(id);
    }

    public void deleteProductByCateogoryId(String id) {
        List<Products> products = productsRepository.findByCategory(id);
        productsRepository.deleteAll(products);


    }

    public List<Products> searchProducts(String query) {
        return productsRepository.findByProductNameOrProductDescription(query);
    }
    public void testProducts() {
        List<String> testIds = List.of("e4838fa3-564f-4914-9e50-56d4fb0c4a06", "1cc5abbf-8520-46c0-84c1-8ec28959d0fe");
        List<Products> products = productsRepository.findByIdIn(testIds);
        System.out.println("DEBUG: Direct query found " + products.toString() + " products.");
    }

    public List<Products> getProductsByIds(List<String> productsIds) {
        if(productsIds == null && productsIds.isEmpty()) {
            return Collections.emptyList();
        }
        return productsRepository.findByIdIn(productsIds);
    }
    public List<Products> getAllProductsByIds(List<String> productIds) {
        return productsRepository.findAllById(productIds);
    }

    public void saveProduct(Products product) {
        productsRepository.save(product);
    }

    public void increaseStock(String productId, int quantity) {
        Products product = getProductById(productId);
        if (product != null) {
            int currentStock = product.getStockQuantityInt();
            product.setStock_quantity(String.valueOf(currentStock + quantity));
            saveProduct(product);
        } else {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
    }

    public void adjustStock(String productId, int adjustment, String notes) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int currentStock = product.getStockQuantityInt();
        int newStock = currentStock + adjustment;

        if(newStock < 0) newStock = 0;

        product.setStock_quantity(String.valueOf(newStock));
        productsRepository.save(product);

        // Record history with correct adjustment value
        inventoryHistoryService.recordInventoryChange(
                productId,
                currentStock,
                newStock,
                adjustment,  // Include actual adjustment value
                notes
        );
    }

    public Products getPrductsByProductName(String productName) {
        return productsRepository.findByProductName(productName);
    }

    public Set<Products> getSetProductsByIds(List<String> ids) {

            if (ids == null || ids.isEmpty()) {
                return Collections.emptySet();
            }
            return new HashSet<>(productsRepository.findAllById(ids));
    }

    public List<Products> findAll() {
        return productsRepository.findAll();
    }

    public Page<Products> getAllProductsByPages(Pageable pageable) {
        return productsRepository.findAll(pageable);
    }
    public Page<Products> getProductsByCategoryId(String categoryId, Pageable pageable) {
        return productsRepository.findByCategory(categoryId, pageable);
    }


    public Page<Products> searchProductsByCategory(String query, String categoryId, Pageable pageable) {
        String searchPattern = ".*" + query.trim().toLowerCase() + ".*";
        return productsRepository.findByCategoryAndSearchQuery(categoryId, searchPattern, pageable);
    }

//    public Products getProductName(String productName) {
//        return productsRepository.findByProductName(productName);
//    }
}
