package com.tabnova.aidashboard.Model;

public class SettingsAppsModel {

    int id;
    int drawableId;
    String settingName;

    public SettingsAppsModel(int id, int drawableId, String settingName) {
        this.id = id;
        this.drawableId = drawableId;
        this.settingName = settingName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }
}
