package com.example.demo.models;

import org.springframework.data.annotation.Id;

enum AddressType {
    HOME, WORK, OTHER
}

public class Address {

    @Id
    private String id;

    private String name;
    private String street;
    private String city;
    private String state;
    private String zip;
    private AddressType place;
    private String userId;

    public Address() {

    }
    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s", street, city, state, zip);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", place='" + place + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public Address(String id, String name, String street, String city, String state, String zip, AddressType place, String userId) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.place = place;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AddressType getPlace() {
        return place;
    }

    public void setPlace(AddressType place) {
        this.place = place;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}