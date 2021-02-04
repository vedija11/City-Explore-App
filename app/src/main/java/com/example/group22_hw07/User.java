package com.example.group22_hw07;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    String first_name, last_name, gender, profile_pic_URL, emailID, password;

    public User() {
    }

    public User(Map<String, Object> userMap) {
        this.first_name = (String) userMap.get("FirstName");
        this.last_name = (String) userMap.get("LastName");
        this.gender = (String) userMap.get("Gender");
        this.profile_pic_URL = (String) userMap.get("ProfilePicURL");
        this.emailID = (String) userMap.get("EmailID");
        this.password = (String) userMap.get("Password");
    }

    public Map toHashMap() {
        Map<String, Object> userMap = new HashMap<>();

        userMap.put("FirstName", this.first_name);
        userMap.put("LastName", this.last_name);
        userMap.put("Gender", this.gender);
        userMap.put("ProfilePicURL", this.profile_pic_URL);
        userMap.put("EmailID", this.emailID);
        userMap.put("Password", this.password);

        return userMap;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfile_pic_URL() {
        return profile_pic_URL;
    }

    public void setProfile_pic_URL(String profile_pic_URL) {
        this.profile_pic_URL = profile_pic_URL;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", gender='" + gender + '\'' +
                ", profile_pic_URL='" + profile_pic_URL + '\'' +
                ", emailID='" + emailID + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
