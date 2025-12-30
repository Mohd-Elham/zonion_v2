package com.example.demo.models;

public class RazorDTO {
    private int amount;
    private String name;
    private String email;
    private String razorpayId

            ;

    public RazorDTO(int amount, String name, String email, String razorpayId) {
        this.amount = amount;
        this.name = name;
        this.email = email;
        this.razorpayId = razorpayId;
    }

    public RazorDTO() {

    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRazorpayId() {
        return razorpayId;
    }

    public void setRazorpayId(String razorpayId) {
        this.razorpayId = razorpayId;
    }

    @Override
    public String toString() {
        return "RazorDTO{" +
                "amount=" + amount +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", razorpayId='" + razorpayId + '\'' +
                '}';
    }
}
