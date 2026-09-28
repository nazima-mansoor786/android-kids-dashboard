package com.tabnova.aidashboard.Model;

import java.io.Serializable;

public class AppModel implements Serializable {

    int id;
    int viewType;
    boolean isSelected, isSystemApp, isWebShortCut, isLocked, isAfterDateInstall, isShowAllApp, isUpgradableApp;
    long appInstallTime;
    String packageDetails;
    String appVersion;
    String appInfo;
    String targetSdk;
    String dateInstalled;
    String appCategory;
    String categoryAppsSize;
    int row;
    int column;
    int position;
    String location;


    public AppModel() {
    }

    public AppModel(String packageDetails) {
        this.packageDetails = packageDetails;
    }

    public AppModel(int id, boolean isSelected, String packageDetails, String appInfo) {
        this.id = id;
        this.isSelected = isSelected;
        this.packageDetails = packageDetails;
        this.appInfo = appInfo;
    }

    public AppModel(int id, boolean isSelected, boolean isSystemApp, String packageDetails, String appVersion, String appInfo, String targetSdk, String dateInstalled) {
        this.id = id;
        this.isSelected = isSelected;
        this.isSystemApp = isSystemApp;
        this.packageDetails = packageDetails;
        this.appVersion = appVersion;
        this.appInfo = appInfo;
        this.targetSdk = targetSdk;
        this.dateInstalled = dateInstalled;
    }

    public AppModel(int id, boolean isSelected, boolean isSystemApp, boolean isWebShortCut, String packageDetails, String appVersion, String appInfo,
                    String targetSdk, String dateInstalled, int row, int column, int position, String location) {
        this.id = id;
        this.isSelected = isSelected;
        this.isSystemApp = isSystemApp;
        this.isWebShortCut = isWebShortCut;
        this.packageDetails = packageDetails;
        this.appVersion = appVersion;
        this.appInfo = appInfo;
        this.targetSdk = targetSdk;
        this.dateInstalled = dateInstalled;
        this.row = row;
        this.column = column;
        this.position = position;
        this.location = location;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(String packageDetails) {
        this.packageDetails = packageDetails;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
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

    public boolean isWebShortCut() {
        return isWebShortCut;
    }

    public void setWebShortCut(boolean webShortCut) {
        isWebShortCut = webShortCut;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getCategoryAppsSize() {
        return categoryAppsSize;
    }

    public void setCategoryAppsSize(String categoryAppsSize) {
        this.categoryAppsSize = categoryAppsSize;
    }

    public boolean isAfterDateInstall() {
        return isAfterDateInstall;
    }

    public void setAfterDateInstall(boolean afterDateInstall) {
        isAfterDateInstall = afterDateInstall;
    }

    public boolean isShowAllApp() {
        return isShowAllApp;
    }

    public void setShowAllApp(boolean showAllApp) {
        isShowAllApp = showAllApp;
    }

    public long getAppInstallTime() {
        return appInstallTime;
    }

    public void setAppInstallTime(long appInstallTime) {
        this.appInstallTime = appInstallTime;
    }

    public boolean isUpgradableApp() {
        return isUpgradableApp;
    }

    public void setUpgradableApp(boolean upgradableApp) {
        isUpgradableApp = upgradableApp;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
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

    @Override
    public String toString() {
        return "AppModel{" +
                "id=" + id +
                ", viewType=" + viewType +
                ", isSelected=" + isSelected +
                ", isSystemApp=" + isSystemApp +
                ", isWebShortCut=" + isWebShortCut +
                ", isLocked=" + isLocked +
                ", isAfterDateInstall=" + isAfterDateInstall +
                ", isShowAllApp=" + isShowAllApp +
                ", isUpgradableApp=" + isUpgradableApp +
                ", appInstallTime=" + appInstallTime +
                ", packageDetails='" + packageDetails + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", appInfo='" + appInfo + '\'' +
                ", targetSdk='" + targetSdk + '\'' +
                ", dateInstalled='" + dateInstalled + '\'' +
                ", appCategory='" + appCategory + '\'' +
                ", categoryAppsSize='" + categoryAppsSize + '\'' +
                ", row='" + row + '\'' +
                ", column='" + column + '\'' +
                ", position='" + position + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
