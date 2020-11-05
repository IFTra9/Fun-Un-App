package com.example.funun.Model;

import android.media.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This class User represent the User Model in firebase database. */

public class User {

    private String username;
    private String userEmail;
    private String userImage;

    public User(String username, String userEmail, String userImage) {

        this.username = username;
        this.userEmail = userEmail;
        this.userImage = userImage;
    }

    public User (){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public HashMap<String, Object> toMapFirst() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", this.username);
        result.put("userEmail", this.userEmail);
        result.put("userImage", this.userImage);
        return result;
    }
}
