package com.example.demo.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class Products {

    @Id
    private String id;

    private String category;
    @Field("product_name")
    private String product_name;
    private String product_description;
    private String stock_quantity;
    private String price;
    private String image_1;
    private String image_2;
    private String image_3;
    private String image_4;

    public Products(String id, String category, String product_name, String product_description, String stock_quantity, String price, String image_1, String image_2, String image_3, String image_4) {
        this.id = id;
        this.category = category;
        this.product_name = product_name;
        this.product_description = product_description;
        this.stock_quantity = stock_quantity;
        this.price = price;
        this.image_1 = image_1;
        this.image_2 = image_2;
        this.image_3 = image_3;
        this.image_4 = image_4;
    }

    public Integer getStockQuantityInt() {
        try{
            return Integer.parseInt(stock_quantity);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    // Products.java
    public void increaseStock(int quantity) {
        try {
            int current = Integer.parseInt(this.stock_quantity);
            this.stock_quantity = String.valueOf(current + quantity);
        } catch (NumberFormatException e) {
            this.stock_quantity = String.valueOf(quantity);
        }
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_description() {
        return product_description;
    }

    public void setProduct_description(String product_description) {
        this.product_description = product_description;
    }

    public String getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(String stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage_1() {
        return image_1;
    }

    public void setImage_1(String image_1) {
        this.image_1 = image_1;
    }

    public String getImage_2() {
        return image_2;
    }

    public void setImage_2(String image_2) {
        this.image_2 = image_2;
    }

    public String getImage_3() {
        return image_3;
    }

    public void setImage_3(String image_3) {
        this.image_3 = image_3;
    }

    public String getImage_4() {
        return image_4;
    }

    public void setImage_4(String image_4) {
        this.image_4 = image_4;
    }

    @Override
    public String toString() {
        return "Products{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", product_name='" + product_name + '\'' +
                ", product_description='" + product_description + '\'' +
                ", stock_quantity='" + stock_quantity + '\'' +
                ", price='" + price + '\'' +
                ", image_1='" + image_1 + '\'' +
                ", image_2='" + image_2 + '\'' +
                ", image_3='" + image_3 + '\'' +
                ", image_4='" + image_4 + '\'' +
                '}';
    }
}
