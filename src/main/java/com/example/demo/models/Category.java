package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "Categories")
public class Category {

    @Id
    private String id;

    @Field("category_name")
    private String categoryName;
    private String category_description;
    private String image_path;

    public Category(String id, String category_name, String category_description, String image_path) {
        this.id = id;
        this.categoryName = category_name;
        this.category_description = category_description;
        this.image_path = image_path;
    }

    public Category() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory_name() {
        return categoryName;
    }

    public void setCategory_name(String category_name) {
        this.categoryName = category_name;
    }

    public String getCategory_description() {
        return category_description;
    }

    public void setCategory_description(String category_description) {
        this.category_description = category_description;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", category_name='" + categoryName + '\'' +
                ", category_description='" + category_description + '\'' +
                ", category_image='" + image_path + '\'' +
                '}';
    }
}
