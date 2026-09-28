package com.tabnova.aidashboard.Model;

public class AppPermissionModel {

    int id;
    int firstTime;
    String permissionName;
    boolean isPermissionGiven;

    public AppPermissionModel(int id, String permissionName, boolean isPermissionGiven) {
        this.id = id;
        this.permissionName = permissionName;
        this.isPermissionGiven = isPermissionGiven;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public boolean isPermissionGiven() {
        return isPermissionGiven;
    }

    public void setPermissionGiven(boolean permissionGiven) {
        isPermissionGiven = permissionGiven;
    }
}
