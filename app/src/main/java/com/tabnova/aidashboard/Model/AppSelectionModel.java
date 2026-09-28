package com.tabnova.aidashboard.Model;

import java.util.ArrayList;

public class AppSelectionModel {
    String categoryName;
    ArrayList<AppModel> appModels;
    boolean isExpand;

    public AppSelectionModel() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ArrayList<AppModel> getAppModels() {
        return appModels;
    }

    public void setAppModels(ArrayList<AppModel> appModels) {
        this.appModels = appModels;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }
}
