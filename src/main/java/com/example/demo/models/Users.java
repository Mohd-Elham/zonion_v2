package com.example.demo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexOptions;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Query;


@Document(collection = "user")
public class Users {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;
    private String email;
    private String phone;
    private String roles;
    private boolean isActive;
    private long created_at;
    private String auth_provider;
    private String referralCode;
    private boolean isReferralCodeUsed = false;
    private boolean referralPromptShown = false;
    private int numberOfReferrals = 0;


    public boolean isReferralCodeUsed() {
        return isReferralCodeUsed;
    }

    public void setReferralCodeUsed(boolean referralCodeUsed) {
        isReferralCodeUsed = referralCodeUsed;
    }

    public boolean isReferralPromptShown() {
        return referralPromptShown;
    }

    public void setReferralPromptShown(boolean referralPromptShown) {
        this.referralPromptShown = referralPromptShown;
    }

    public int getNumberOfReferrals() {
        return numberOfReferrals;
    }

    public void setNumberOfReferrals(int numberOfReferrals) {
        this.numberOfReferrals = numberOfReferrals;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }


    public Users() {
    }

    public Users(String id, String username, String password,
                 String email, String phone, String roles,
                 boolean isActive, long created_at, String auth_provider, String referralCode) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.roles = roles;
        this.isActive = isActive;
        this.created_at = created_at;
        this.auth_provider = auth_provider;
        this.referralCode = referralCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", roles='" + roles + '\'' +
                ", is_active=" + isActive +
                ", created_at=" + created_at +
                '}';
    }


    public String getAuth_provider() {
        return auth_provider;
    }

    public void setAuth_provider(String auth_provider) {
        this.auth_provider = auth_provider;
    }
}
