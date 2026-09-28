package com.tabnova.aidashboard.Model;

public class PredefineFoldersModel {
    private int id;
    private int drawableId;
    private String folderName;

    public PredefineFoldersModel(int id, int drawableId, String folderName) {
        this.id = id;
        this.drawableId = drawableId;
        this.folderName = folderName;
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
