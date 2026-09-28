package com.tabnova.aidashboard.Model;

import java.io.Serializable;

public class ProfileListModel implements Serializable {
    int id;
    String profileName;
    String profileId;

    public ProfileListModel(int id, String profileName, String profileId) {
        this.id = id;
        this.profileName = profileName;
        this.profileId = profileId;
    }

    public ProfileListModel() {
    }

    public ProfileListModel(int id, String profileName) {
        this.id = id;
        this.profileName = profileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return profileName;
    }
}
