package com.tabnova.aidashboard.Model;

import java.util.ArrayList;

public class SettingsModel {

    int id;
    int drawableId;
    String settingName;
    int type;
    ArrayList<SettingsAppsModel> settingsAppsModels;

    public SettingsModel(int id, int drawableId, String settingName) {
        this.id = id;
        this.drawableId = drawableId;
        this.settingName = settingName;
    }

    public SettingsModel(String settingName, ArrayList<SettingsAppsModel> settingsAppsModels) {
        this.settingName = settingName;
        this.settingsAppsModels = settingsAppsModels;
    }

    public SettingsModel(int id, int type, ArrayList<SettingsAppsModel> settingsAppsModels) {
        this.id = id;
        this.type = type;
        this.settingsAppsModels = settingsAppsModels;
    }

    public ArrayList<SettingsAppsModel> getSettingsAppsModels() {
        return settingsAppsModels;
    }

    public void setSettingsAppsModels(ArrayList<SettingsAppsModel> settingsAppsModels) {
        this.settingsAppsModels = settingsAppsModels;
    }

    public SettingsModel(int type) {
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
