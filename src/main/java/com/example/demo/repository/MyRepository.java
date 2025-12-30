package com.example.demo.repository;

import com.example.demo.models.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyRepository extends MongoRepository<Category, String> {

    Category findByCategoryName(String name);
}
