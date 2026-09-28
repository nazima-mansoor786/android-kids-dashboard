package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AppCategoryModel implements Serializable {

    String packageName,appCategory;

    public AppCategoryModel() {
    }

    public AppCategoryModel( String packageName, String appCategory) {
        this.packageName = packageName;
        this.appCategory = appCategory;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    @NonNull
    @Override
    public String toString() {
        return "Employee [packageName=" +packageName+ ", appCategory=" +  appCategory +"]";
    }
}
