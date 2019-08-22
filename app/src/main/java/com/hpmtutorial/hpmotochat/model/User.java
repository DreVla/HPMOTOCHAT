package com.hpmtutorial.hpmotochat.model;

public class User {

    public String uid;
    public String email;

    public User() {
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public User(String emailValue) {
        this.email = emailValue;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
