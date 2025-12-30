package com.example.demo.service;

import com.example.demo.models.Category;
import com.example.demo.repository.MyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {

    @Autowired
    MyRepository myRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public CategoryService(MyRepository myRepository) {
        this.myRepository = myRepository;
    }

//    public void saveToCategory(Category category) {
//        myRepository.save(category);
//    }

    public void saveToCategory(String name, String description, String savedImagePath) {
        Category category = new Category();
        String randomeUID = UUID.randomUUID().toString();
        System.out.println("the random uid is "+randomeUID);
        category.setId(randomeUID);
        category.setCategory_name(name);
        category.setCategory_description(description);
        category.setImage_path(savedImagePath);
        myRepository.save(category);

        Query query = new Query();
        ; // Include specific fields

        System.out.println("saved the category" + mongoTemplate.find(query, Category.class));

    }

    public List<Category> getAllCategory() {
        System.out.println("this is repositary data "+myRepository.findAll());
        return myRepository.findAll();
    }

    public void deleteCategory(String id) {
        myRepository.deleteById(id);
    }

    public Category getCategoryByid(String id) {
        Optional<Category> user = myRepository.findById(id);
        if(user.isPresent()) {
            return user.get();
        }
        throw new RuntimeException("Category not found");

    }

    public void updateCategory(String id, String name, String description, String imagePath) {
        Category category = getCategoryByid(id);
        category.setCategory_name(name);
        category.setCategory_description(description);
        if(imagePath != null) {
            category.setImage_path(imagePath);
        }
        myRepository.save(category);
    }

    public Category getCategoryById(String id) {
        return myRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("category not found"));
    }

    public Category getCategoryByName(String name) {
        return myRepository.findByCategoryName(name);
    }

    public Set<Category> getCategoriesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(myRepository.findAllById(ids));
    }

    public List<Category> findAll() {
        return myRepository.findAll();
    }

    public List<Category> getCategoriesByIds2(Set<String> categoryIds) {
        return myRepository.findAllById(categoryIds);
    }
}