package com.example.demo.repository;

import com.example.demo.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductsRepository extends MongoRepository<Products, String> {
    List<Products> findByCategory(String id);

    @Query("{'$or': ["
            + "{'product_name': {$regex: ?0, $options: 'i'}}, "
            + "{'product_description': {$regex: ?0, $options: 'i'}}"
            + "]}")
    List<Products> findByProductNameOrProductDescription(String searchText);

    List<Products> findByIdIn(List<String> ids);

    @Query("{'product_name': ?0}")
    Products findByProductName(String productName);


    Page<Products> findByCategory(String id, Pageable pageable);


    @Query("{'$and': ["
            + "{'category': ?0}, "
            + "{'$or': ["
            + "{'product_name': {$regex: ?1, $options: 'i'}}, "
            + "{'product_description': {$regex: ?1, $options: 'i'}}"
            + "]}]}")
    Page<Products> findByCategoryAndSearchQuery(String categoryId, String searchPattern, Pageable pageable);
}
