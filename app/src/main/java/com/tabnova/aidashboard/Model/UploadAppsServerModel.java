package com.tabnova.aidashboard.Model;

public class UploadAppsServerModel {

    int id;
    int isSelected;
    int appVersionCode;
    boolean isLocked;
    String appName;
    String packageName;
    String appVersion;
    String appInfo;
    String targetSdk;
    String dateInstalled;
    String appCategory;
    int grid_row;
    int grid_column;
    int position;
    String location;

    public UploadAppsServerModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }

    public String getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(String targetSdk) {
        this.targetSdk = targetSdk;
    }

    public String getDateInstalled() {
        return dateInstalled;
    }

    public void setDateInstalled(String dateInstalled) {
        this.dateInstalled = dateInstalled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }

    public int getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(int appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public int getGrid_row() {
        return grid_row;
    }

    public void setGrid_row(int grid_row) {
        this.grid_row = grid_row;
    }

    public int getGrid_column() {
        return grid_column;
    }

    public void setGrid_column(int grid_column) {
        this.grid_column = grid_column;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
