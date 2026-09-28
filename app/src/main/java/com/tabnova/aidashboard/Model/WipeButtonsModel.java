package com.tabnova.aidashboard.Model;

import java.io.Serializable;

public class WipeButtonsModel implements Serializable {
    int id;
    int status;
    String settingName;
    boolean isSelected = false;

    public WipeButtonsModel(int id, int status, String settingName, boolean isSelected) {
        this.id = id;
        this.status = status;
        this.settingName = settingName;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WipeButtonsModel{" +
                "id=" + id +
                ", status=" + status +
                ", settingName='" + settingName + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
