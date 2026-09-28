package com.tabnova.aidashboard.Model;

public class LocationModel {
    private int id;
    private String latitude;
    private String longitude;
    private boolean isUpload;

    public LocationModel() {
    }

    public LocationModel(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}