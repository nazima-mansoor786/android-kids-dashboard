package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AddedWebShortcutModel implements Serializable {
    int id;
    String webSiteName;
    String webSiteUrl;
    String websiteIcon;
    boolean isSelected;
    int row;
    int column;
    int position;
    String location;

    public AddedWebShortcutModel(int id, String webSiteName, String webSiteUrl, String websiteIcon, boolean isSelected, int row, int column, int position, String location) {
        this.id = id;
        this.webSiteName = webSiteName;
        this.webSiteUrl = webSiteUrl;
        this.websiteIcon = websiteIcon;
        this.isSelected = isSelected;
        this.row = row;
        this.column = column;
        this.position = position;
        this.location = location;
    }

    public AddedWebShortcutModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWebSiteName() {
        return webSiteName;
    }

    public void setWebSiteName(String webSiteName) {
        this.webSiteName = webSiteName;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getWebsiteIcon() {
        return websiteIcon;
    }

    public void setWebsiteIcon(String websiteIcon) {
        this.websiteIcon = websiteIcon;
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


    @NonNull
    @Override
    public String toString() {
        return "History [id=" + id + ", webSiteName=" + webSiteName + ", webSiteUrl=" + webSiteUrl + ", websiteIcon=" + websiteIcon + ", row=" + row + ", column=" + column + ", position=" + position + ", location=" + location + ", isSelected=" + isSelected + "]";
    }
}
