package com.example.group22_hw07;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.type.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TripData implements Serializable {
    String TripID;
    String MessageCollectionID;
    String CreatedBy;
    String TripName;
    String TripDescription;
    ArrayList<String> Location;
    String PhotoURL;
    ArrayList<String> UIDs;

    @Override
    public String toString() {
        return "TripData{" +
                "TripID='" + TripID + '\'' +
                ", MessageCollectionID='" + MessageCollectionID + '\'' +
                ", CreatedBy='" + CreatedBy + '\'' +
                ", TripName='" + TripName + '\'' +
                ", TripDescription='" + TripDescription + '\'' +
                ", Location=" + Location +
                ", PhotoURL='" + PhotoURL + '\'' +
                ", UIDs=" + UIDs +
                '}';
    }

    public TripData() {
    }

    public TripData(Map<String, Object> userMap) {
        this.TripID = (String) userMap.get("TripID");
        this.MessageCollectionID = (String) userMap.get("MessageCollectionID");
        this.CreatedBy = (String) userMap.get("CreatedBy");
        this.TripName = (String) userMap.get("TripName");
        this.Location = (ArrayList<String>) userMap.get("Location");
        this.UIDs = (ArrayList<String>) userMap.get("UIDs");
        this.PhotoURL = (String) userMap.get("PhotoURL");
        this.TripDescription = (String) userMap.get("TripDescription");
    }

    public Map toHashMap() {
        Map<String, Object> userMap = new HashMap<>();

        userMap.put("TripID", this.TripID);
        userMap.put("MessageCollectionID", this.MessageCollectionID);
        userMap.put("CreatedBy", this.CreatedBy);
        userMap.put("TripName", this.TripName);
        userMap.put("Location", this.Location);
        userMap.put("UIDs", this.UIDs);
        userMap.put("PhotoURL", this.PhotoURL);
        userMap.put("TripDescription", this.TripDescription);

        return userMap;
    }

    public String getMessageCollectionID() {
        return MessageCollectionID;
    }

    public void setMessageCollectionID(String messageCollectionID) {
        MessageCollectionID = messageCollectionID;
    }

    public ArrayList<String> getUIDs() {
        return UIDs;
    }

    public void setUIDs(ArrayList<String> UIDs) {
        this.UIDs = UIDs;
    }

    public String getTripDescription() {
        return TripDescription;
    }

    public void setTripDescription(String tripDescription) {
        TripDescription = tripDescription;
    }

    public String getTripID() {
        return TripID;
    }

    public void setTripID(String tripID) {
        TripID = tripID;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public String getTripName() {
        return TripName;
    }

    public void setTripName(String tripName) {
        TripName = tripName;
    }

    public ArrayList<String> getLocation() {
        return Location;
    }

    public void setLocation(ArrayList<String> location) {
        Location = location;
    }

    public String getPhotoURL() {
        return PhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        PhotoURL = photoURL;
    }
}
