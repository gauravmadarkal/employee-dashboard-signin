package com.betsol.employeePortal.model;


import org.springframework.stereotype.Component;
import javax.persistence.*;

@Component
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "userid",length = 36)
    private String userId;

    @Column(name = "username")
    private String displayName;

    @Column(name = "email")
    private String mail;

    @Column(name = "isadmin")
    private boolean isAdmin=false;

    @Transient
    private String accessToken;

    @Transient
    private String id;


    public User() {
    }

    public User(String userId, String displayName, String mail, boolean isAdmin, String accessToken,String id) {
        this.userId = userId;
        this.displayName = displayName;
        this.mail = mail;
        this.isAdmin = isAdmin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
