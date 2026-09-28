package com.tabnova.aidashboard.Model;

public class TempBlockAppsModel {
    int isAddedDashBoard;
    int isFromBackGround;
    String packageName;

    public TempBlockAppsModel(int isAddedDashBoard, int isFromBackGround, String packageName) {
        this.isAddedDashBoard = isAddedDashBoard;
        this.isFromBackGround = isFromBackGround;
        this.packageName = packageName;
    }

    public int getIsAddedDashBoard() {
        return isAddedDashBoard;
    }

    public void setIsAddedDashBoard(int isAddedDashBoard) {
        this.isAddedDashBoard = isAddedDashBoard;
    }

    public int getIsFromBackGround() {
        return isFromBackGround;
    }

    public void setIsFromBackGround(int isFromBackGround) {
        this.isFromBackGround = isFromBackGround;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
